package id.myitsbroadcast.migration;

import id.myitsbroadcast.config.AppConfig;
import org.sqlite.SQLiteConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * One-way, id-preserving import from the legacy Laravel SQLite database into
 * PostgreSQL.
 *
 * <p>The source database is opened read-only. The destination is only touched
 * when {@code --dry-run} is absent, and even then each table is upserted by
 * primary key inside a single transaction. Event registrations, sessions,
 * caches and jobs are deliberately ignored: they are not part of the JSP
 * application, and no RSVP/attendee functionality is being migrated.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * java -jar target/myitsbroadcast-importer.jar --dry-run
 * java -jar target/myitsbroadcast-importer.jar
 * java -jar target/myitsbroadcast-importer.jar --replace
 * </pre>
 */
public final class Importer {

    private static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");
    private static final DateTimeFormatter SPACE_DATE =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    private Importer() {
    }

    public static void main(String[] args) {
        Options options = Options.parse(args);

        if (options.help) {
            printUsage();
            return;
        }

        try {
            run(options);
        } catch (Exception exception) {
            System.err.println("Import failed: " + exception.getMessage());
            exception.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void run(Options options) throws Exception {
        Path source = Path.of(options.source).toAbsolutePath().normalize();

        if (!Files.isRegularFile(source)) {
            throw new IllegalStateException("SQLite source not found: " + source);
        }

        Counts counts;

        try (Connection sqlite = openSource(source)) {
            counts = readCounts(sqlite);
            printSource(source, counts, options.dryRun);

            if (options.dryRun) {
                System.out.println();
                System.out.println("Dry run complete. No PostgreSQL connection was opened and nothing was written.");
                return;
            }

            String url = AppConfig.databaseUrl();
            String user = AppConfig.databaseUser();

            if (AppConfig.databasePassword().isBlank()) {
                throw new IllegalStateException(
                        "DB_PASSWORD is not set. Export the PostgreSQL credentials before importing.");
            }

            System.out.println();
            System.out.println("Destination: " + safeUrl(url));

            try (Connection postgres = DriverManager.getConnection(url, user, AppConfig.databasePassword())) {
                postgres.setAutoCommit(false);

                try {
                    if (options.replace) {
                        clearDestination(postgres);
                    }

                    importUsers(sqlite, postgres);
                    importCategories(sqlite, postgres);
                    importPosts(sqlite, postgres);
                    importComments(sqlite, postgres);
                    importBookmarks(sqlite, postgres);
                    resetSequences(postgres);
                    verifyCounts(sqlite, postgres, counts);

                    postgres.commit();
                } catch (Exception exception) {
                    postgres.rollback();
                    throw exception;
                }
            }
        }

        copyAssets(Path.of(options.assets).toAbsolutePath().normalize(), options.skipAssets);

        System.out.println();
        System.out.println("Import committed. Source database was never modified.");
        System.out.println("Excluded by design: event_registrations, passkeys, sessions, cache, jobs,");
        System.out.println("                   failed_jobs, migrations, password reset and email verification.");
    }

    private static Connection openSource(Path source) throws SQLException {
        loadDrivers();

        SQLiteConfig config = new SQLiteConfig();
        config.setReadOnly(true);

        return DriverManager.getConnection("jdbc:sqlite:" + source, config.toProperties());
    }

    /**
     * The assembly flattens dependency jars, so only one
     * {@code META-INF/services/java.sql.Driver} entry survives. Register both
     * drivers explicitly rather than relying on service discovery.
     */
    private static void loadDrivers() {
        try {
            Class.forName("org.sqlite.JDBC");
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("JDBC drivers are missing from the importer jar.", exception);
        }
    }

    private static Counts readCounts(Connection sqlite) throws SQLException {
        return new Counts(
                count(sqlite, "SELECT count(*) FROM users"),
                count(sqlite, "SELECT count(*) FROM categories"),
                count(sqlite, "SELECT count(*) FROM posts"),
                count(sqlite, "SELECT count(*) FROM comments"),
                count(sqlite, "SELECT count(*) FROM bookmarks"),
                count(sqlite, "SELECT count(*) FROM event_registrations"));
    }

    private static long count(Connection connection, String sql) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() ? resultSet.getLong(1) : 0L;
        } catch (SQLException exception) {
            return 0L;
        }
    }

    private static void printSource(Path source, Counts counts, boolean dryRun) {
        System.out.println("myITS Broadcast — SQLite to PostgreSQL importer");
        System.out.println("Source: " + source);
        System.out.println("Mode:   " + (dryRun ? "dry run (read-only)" : "import"));
        System.out.println();
        System.out.printf("  users              %,d%n", counts.users);
        System.out.printf("  categories         %,d%n", counts.categories);
        System.out.printf("  posts              %,d%n", counts.posts);
        System.out.printf("  comments           %,d%n", counts.comments);
        System.out.printf("  bookmarks          %,d%n", counts.bookmarks);
        System.out.printf("  event registrations %,d (skipped by design)%n", counts.eventRegistrations);
    }

    private static void clearDestination(Connection postgres) throws SQLException {
        try (Statement statement = postgres.createStatement()) {
            statement.executeUpdate("DELETE FROM bookmarks");
            statement.executeUpdate("DELETE FROM comments");
            statement.executeUpdate("DELETE FROM posts");
            statement.executeUpdate("DELETE FROM categories");
            statement.executeUpdate("DELETE FROM users");
        }

        System.out.println("Destination rows cleared (--replace).");
    }

    private static void importUsers(Connection sqlite, Connection postgres) throws SQLException {
        String select = "SELECT id, name, email, password, remember_token, is_moderator, "
                + "avatar_path, created_at, updated_at FROM users ORDER BY id";
        String upsert = """
                INSERT INTO users (id, name, email, password, remember_token, is_moderator,
                                   avatar_path, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    email = EXCLUDED.email,
                    password = EXCLUDED.password,
                    remember_token = EXCLUDED.remember_token,
                    is_moderator = EXCLUDED.is_moderator,
                    avatar_path = EXCLUDED.avatar_path,
                    updated_at = EXCLUDED.updated_at
                """;

        long written = 0;

        try (Statement statement = sqlite.createStatement();
             ResultSet rows = statement.executeQuery(select);
             PreparedStatement insert = postgres.prepareStatement(upsert)) {

            while (rows.next()) {
                insert.setLong(1, rows.getLong("id"));
                insert.setString(2, rows.getString("name"));
                insert.setString(3, rows.getString("email"));
                insert.setString(4, rows.getString("password"));
                insert.setString(5, rows.getString("remember_token"));
                insert.setBoolean(6, rows.getInt("is_moderator") == 1);
                insert.setString(7, rows.getString("avatar_path"));
                insert.setTimestamp(8, timestamp(rows.getString("created_at")));
                insert.setTimestamp(9, timestamp(rows.getString("updated_at")));
                insert.executeUpdate();
                written++;
            }
        }

        System.out.printf("Users imported: %,d (email_verified_at and 2FA columns ignored)%n", written);
    }

    private static void importCategories(Connection sqlite, Connection postgres) throws SQLException {
        String upsert = """
                INSERT INTO categories (id, name, slug, description, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    slug = EXCLUDED.slug,
                    description = EXCLUDED.description,
                    updated_at = EXCLUDED.updated_at
                """;

        long written = 0;

        try (Statement statement = sqlite.createStatement();
             ResultSet rows = statement.executeQuery(
                     "SELECT id, name, slug, description, created_at, updated_at FROM categories ORDER BY id");
             PreparedStatement insert = postgres.prepareStatement(upsert)) {

            while (rows.next()) {
                insert.setLong(1, rows.getLong("id"));
                insert.setString(2, rows.getString("name"));
                insert.setString(3, rows.getString("slug"));
                insert.setString(4, rows.getString("description"));
                insert.setTimestamp(5, timestamp(rows.getString("created_at")));
                insert.setTimestamp(6, timestamp(rows.getString("updated_at")));
                insert.executeUpdate();
                written++;
            }
        }

        System.out.printf("Categories imported: %,d%n", written);
    }

    private static void importPosts(Connection sqlite, Connection postgres) throws SQLException {
        String upsert = """
                INSERT INTO posts (id, user_id, category_id, type, status, title, slug, body,
                                   cover_image_path, is_pinned, published_at, event_starts_at,
                                   event_ends_at, event_location, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    user_id = EXCLUDED.user_id,
                    category_id = EXCLUDED.category_id,
                    type = EXCLUDED.type,
                    status = EXCLUDED.status,
                    title = EXCLUDED.title,
                    slug = EXCLUDED.slug,
                    body = EXCLUDED.body,
                    cover_image_path = EXCLUDED.cover_image_path,
                    is_pinned = EXCLUDED.is_pinned,
                    published_at = EXCLUDED.published_at,
                    event_starts_at = EXCLUDED.event_starts_at,
                    event_ends_at = EXCLUDED.event_ends_at,
                    event_location = EXCLUDED.event_location,
                    updated_at = EXCLUDED.updated_at
                """;

        long written = 0;

        try (Statement statement = sqlite.createStatement();
             ResultSet rows = statement.executeQuery("""
                     SELECT id, user_id, category_id, type, status, title, slug, body,
                            cover_image_path, is_pinned, published_at, event_starts_at,
                            event_ends_at, event_location, created_at, updated_at
                     FROM posts ORDER BY id
                     """);
             PreparedStatement insert = postgres.prepareStatement(upsert)) {

            while (rows.next()) {
                insert.setLong(1, rows.getLong("id"));
                insert.setLong(2, rows.getLong("user_id"));
                insert.setLong(3, rows.getLong("category_id"));
                insert.setString(4, rows.getString("type"));
                insert.setString(5, rows.getString("status"));
                insert.setString(6, rows.getString("title"));
                insert.setString(7, rows.getString("slug"));
                insert.setString(8, rows.getString("body"));
                insert.setString(9, rows.getString("cover_image_path"));
                insert.setBoolean(10, rows.getInt("is_pinned") == 1);
                insert.setTimestamp(11, nullableTimestamp(rows.getString("published_at")));
                insert.setTimestamp(12, nullableTimestamp(rows.getString("event_starts_at")));
                insert.setTimestamp(13, nullableTimestamp(rows.getString("event_ends_at")));
                insert.setString(14, rows.getString("event_location"));
                insert.setTimestamp(15, timestamp(rows.getString("created_at")));
                insert.setTimestamp(16, timestamp(rows.getString("updated_at")));
                insert.executeUpdate();
                written++;
            }
        }

        System.out.printf("Posts imported: %,d (drafts and archived rows preserved)%n", written);
    }

    /**
     * Insert every comment with a null parent first, then attach replies. This
     * avoids ordering assumptions and keeps the source ids intact even when a
     * reply appears before its parent in the source table.
     */
    private static void importComments(Connection sqlite, Connection postgres) throws SQLException {
        String insert = """
                INSERT INTO comments (id, post_id, user_id, parent_id, body, created_at, updated_at)
                VALUES (?, ?, ?, NULL, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    post_id = EXCLUDED.post_id,
                    user_id = EXCLUDED.user_id,
                    body = EXCLUDED.body,
                    updated_at = EXCLUDED.updated_at
                """;

        List<long[]> parents = new ArrayList<>();
        long written = 0;

        try (Statement statement = sqlite.createStatement();
             ResultSet rows = statement.executeQuery("""
                     SELECT id, post_id, user_id, parent_id, body, created_at, updated_at
                     FROM comments ORDER BY id
                     """);
             PreparedStatement insertStatement = postgres.prepareStatement(insert)) {

            while (rows.next()) {
                long id = rows.getLong("id");
                insertStatement.setLong(1, id);
                insertStatement.setLong(2, rows.getLong("post_id"));
                insertStatement.setLong(3, rows.getLong("user_id"));
                insertStatement.setString(4, rows.getString("body"));
                insertStatement.setTimestamp(5, timestamp(rows.getString("created_at")));
                insertStatement.setTimestamp(6, timestamp(rows.getString("updated_at")));
                insertStatement.executeUpdate();

                long parentId = rows.getLong("parent_id");
                boolean parentNull = rows.wasNull();

                if (!parentNull) {
                    parents.add(new long[]{id, parentId});
                }

                written++;
            }
        }

        try (PreparedStatement update = postgres.prepareStatement(
                "UPDATE comments SET parent_id = ? WHERE id = ?")) {
            for (long[] parent : parents) {
                update.setLong(1, parent[1]);
                update.setLong(2, parent[0]);
                update.addBatch();
            }

            update.executeBatch();
        }

        System.out.printf("Comments imported: %,d (%,d replies preserved)%n", written, parents.size());
    }

    private static void importBookmarks(Connection sqlite, Connection postgres) throws SQLException {
        String upsert = """
                INSERT INTO bookmarks (id, user_id, post_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT DO NOTHING
                """;

        long written = 0;

        try (Statement statement = sqlite.createStatement();
             ResultSet rows = statement.executeQuery(
                     "SELECT id, user_id, post_id, created_at, updated_at FROM bookmarks ORDER BY id");
             PreparedStatement insert = postgres.prepareStatement(upsert)) {

            while (rows.next()) {
                insert.setLong(1, rows.getLong("id"));
                insert.setLong(2, rows.getLong("user_id"));
                insert.setLong(3, rows.getLong("post_id"));
                insert.setTimestamp(4, timestamp(rows.getString("created_at")));
                insert.setTimestamp(5, timestamp(rows.getString("updated_at")));
                written += insert.executeUpdate();
            }
        }

        System.out.printf("Bookmarks imported: %,d (duplicate pairs ignored)%n", written);
    }

    private static void resetSequences(Connection postgres) throws SQLException {
        String[] tables = {"users", "categories", "posts", "comments", "bookmarks"};

        try (Statement statement = postgres.createStatement()) {
            for (String table : tables) {
                statement.execute("""
                        SELECT setval(
                            pg_get_serial_sequence('%s', 'id'),
                            COALESCE((SELECT max(id) FROM %s), 0) + 1,
                            false)
                        """.formatted(table, table));
            }
        }

        System.out.println("PostgreSQL id sequences advanced to the imported maximum.");
    }

    private static void verifyCounts(Connection sqlite, Connection postgres, Counts counts) throws SQLException {
        long users = count(postgres, "SELECT count(*) FROM users");
        long categories = count(postgres, "SELECT count(*) FROM categories");
        long posts = count(postgres, "SELECT count(*) FROM posts");
        long comments = count(postgres, "SELECT count(*) FROM comments");
        long bookmarks = count(postgres, "SELECT count(*) FROM bookmarks");

        System.out.println();
        System.out.println("Verification (source vs destination):");
        System.out.printf("  users       %,d / %,d%n", counts.users, users);
        System.out.printf("  categories  %,d / %,d%n", counts.categories, categories);
        System.out.printf("  posts       %,d / %,d%n", counts.posts, posts);
        System.out.printf("  comments    %,d / %,d%n", counts.comments, comments);
        System.out.printf("  bookmarks   %,d / %,d%n", counts.bookmarks, bookmarks);
    }

    private static void copyAssets(Path sourceRoot, boolean skipAssets) {
        if (skipAssets) {
            System.out.println("Asset copy skipped (--skip-assets).");
            return;
        }

        if (!Files.isDirectory(sourceRoot)) {
            System.out.println("Asset directory not found; cover and avatar files were not copied: " + sourceRoot);
            return;
        }

        Path targetRoot = AppConfig.storageDir().toAbsolutePath().normalize();
        int[] result;

        try {
            result = copyTree(sourceRoot, targetRoot);
        } catch (IOException exception) {
            System.out.println("Asset copy failed: " + exception.getMessage());
            return;
        }

        System.out.printf("Assets copied: %,d file(s), %,d already present. Target: %s%n",
                result[0], result[1], targetRoot);
    }

    private static int[] copyTree(Path sourceRoot, Path targetRoot) throws IOException {
        int copied = 0;
        int present = 0;

        try (var stream = Files.walk(sourceRoot)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();

            for (Path source : files) {
                Path relative = sourceRoot.relativize(source);

                if (relative.getFileName().toString().equals(".gitignore")) {
                    continue;
                }

                Path target = targetRoot.resolve(relative).normalize();

                if (!target.startsWith(targetRoot)) {
                    continue;
                }

                if (Files.exists(target)) {
                    present++;
                    continue;
                }

                Files.createDirectories(target.getParent());
                Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
                copied++;
            }
        }

        return new int[]{copied, present};
    }

    private static Timestamp timestamp(String value) {
        Timestamp parsed = nullableTimestamp(value);

        return parsed == null
                ? Timestamp.valueOf(LocalDateTime.now(ZONE))
                : parsed;
    }

    private static Timestamp nullableTimestamp(String value) {
        LocalDateTime parsed = parse(value);

        return parsed == null ? null : Timestamp.valueOf(parsed);
    }

    static LocalDateTime parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();

        try {
            return LocalDateTime.parse(trimmed, SPACE_DATE);
        } catch (DateTimeParseException ignored) {
            // Fall through to ISO-8601.
        }

        try {
            return LocalDateTime.parse(trimmed);
        } catch (DateTimeParseException ignored) {
            // Fall through to a date-only value.
        }

        try {
            return LocalDate.parse(trimmed).atTime(LocalTime.NOON);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static String safeUrl(String url) {
        int query = url.indexOf('?');

        return query < 0 ? url : url.substring(0, query);
    }

    private static void printUsage() {
        System.out.println("""
                myITS Broadcast — SQLite to PostgreSQL importer

                Options:
                  --source <path>   Legacy SQLite file (default: database/database.sqlite)
                  --assets <path>   Uploaded files root (default: storage/app/public)
                  --dry-run         Read the source and report counts only
                  --replace         Delete existing application rows first (explicitly destructive)
                  --skip-assets     Do not copy cover images and avatars
                  --help            Show this message

                Credentials are read from DB_URL, DB_USER and DB_PASSWORD only.
                The source SQLite database is always opened read-only.
                """);
    }

    private record Counts(long users, long categories, long posts, long comments,
                          long bookmarks, long eventRegistrations) {
    }

    private static final class Options {
        private String source = "database/database.sqlite";
        private String assets = "storage/app/public";
        private boolean dryRun;
        private boolean replace;
        private boolean skipAssets;
        private boolean help;

        static Options parse(String[] args) {
            Options options = new Options();

            for (int index = 0; index < args.length; index++) {
                switch (args[index]) {
                    case "--source" -> options.source = requireValue(args, ++index, "--source");
                    case "--assets" -> options.assets = requireValue(args, ++index, "--assets");
                    case "--dry-run" -> options.dryRun = true;
                    case "--replace" -> options.replace = true;
                    case "--skip-assets" -> options.skipAssets = true;
                    case "--help", "-h" -> options.help = true;
                    default -> throw new IllegalArgumentException("Unknown option: " + args[index]);
                }
            }

            return options;
        }

        private static String requireValue(String[] args, int index, String option) {
            if (index >= args.length || args[index].startsWith("--")) {
                throw new IllegalArgumentException(option + " requires a value.");
            }

            return args[index];
        }
    }
}
