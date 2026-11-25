package id.myitsbroadcast.dao;

import id.myitsbroadcast.config.Db;
import id.myitsbroadcast.model.Category;
import id.myitsbroadcast.model.Page;
import id.myitsbroadcast.model.Post;
import id.myitsbroadcast.model.PostStatus;
import id.myitsbroadcast.model.PostType;
import id.myitsbroadcast.model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostDao {

    private static final String COLUMNS = """
            SELECT p.id, p.user_id, p.category_id, p.type, p.status, p.title, p.slug, p.body,
                   p.cover_image_path, p.is_pinned, p.published_at, p.event_starts_at,
                   p.event_ends_at, p.event_location, p.created_at, p.updated_at,
                   u.name AS author_name, u.email AS author_email, u.avatar_path AS author_avatar,
                   u.is_moderator AS author_moderator,
                   c.name AS category_name, c.slug AS category_slug,
                   (SELECT count(*) FROM comments cm WHERE cm.post_id = p.id) AS comment_count,
                   (SELECT count(*) FROM bookmarks bm WHERE bm.post_id = p.id) AS bookmark_count
            """;

    private static final String FROM = """
            FROM posts p
            JOIN users u ON u.id = p.user_id
            JOIN categories c ON c.id = p.category_id
            """;

    private static final String SELECT = COLUMNS + FROM;

    /** Public feed query with keyword, category and type filters. */
    public Page<Post> publishedPage(String keyword, String categorySlug, PostType type,
                                    int requestedPage, int perPage, Long viewerId) {
        int page = Math.max(1, requestedPage);

        StringBuilder where = new StringBuilder(
                " WHERE p.status = 'published' AND p.published_at IS NOT NULL AND p.published_at <= ? ");
        List<Object> params = new ArrayList<>();
        params.add(LocalDateTime.now(java.time.ZoneId.of(id.myitsbroadcast.config.AppConfig.APP_TIMEZONE)));

        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (p.title ILIKE ? OR p.body ILIKE ?) ");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (categorySlug != null && !categorySlug.isBlank()) {
            where.append(" AND c.slug = ? ");
            params.add(categorySlug);
        }

        if (type != null) {
            where.append(" AND p.type = ? ");
            params.add(type.getValue());
        }

        String countSql = "SELECT count(*) FROM posts p JOIN categories c ON c.id = p.category_id" + where;

        String sql = COLUMNS
                + (viewerId == null
                        ? ""
                        : ", EXISTS (SELECT 1 FROM bookmarks b WHERE b.post_id = p.id AND b.user_id = ?) AS bookmarked\n")
                + FROM
                + where
                + " ORDER BY p.published_at DESC, p.id DESC LIMIT ? OFFSET ?";

        try (Connection connection = Db.connection()) {
            long total = Jdbc.count(connection, countSql, params.toArray());

            List<Object> listParams = new ArrayList<>();

            if (viewerId != null) {
                listParams.add(viewerId);
            }

            listParams.addAll(params);
            listParams.add(perPage);
            listParams.add((long) (page - 1) * perPage);

            List<Post> posts = Jdbc.list(connection, sql, PostDao::map, listParams.toArray());

            return new Page<>(posts, total, page, perPage);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load the feed.", exception);
        }
    }

    public Post findBySlug(String slug, Long viewerId) {
        String bookmarkSelect = viewerId == null
                ? "FALSE AS bookmarked"
                : "EXISTS (SELECT 1 FROM bookmarks b WHERE b.post_id = p.id AND b.user_id = ?) AS bookmarked";

        String sql = COLUMNS + ", " + bookmarkSelect + "\n" + FROM + " WHERE p.slug = ?";

        try (Connection connection = Db.connection()) {
            if (viewerId == null) {
                return Jdbc.one(connection, sql, PostDao::map, slug);
            }

            return Jdbc.one(connection, sql, PostDao::map, viewerId, slug);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load the broadcast.", exception);
        }
    }

    public Post findById(long id) {
        try (Connection connection = Db.connection()) {
            return Jdbc.one(connection, SELECT + " WHERE p.id = ?", PostDao::map, id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load the broadcast.", exception);
        }
    }

    public boolean slugExists(String slug, Long excludeId) {
        String sql = "SELECT count(*) FROM posts WHERE slug = ?"
                + (excludeId == null ? "" : " AND id <> ?");

        try (Connection connection = Db.connection()) {
            long count = excludeId == null
                    ? Jdbc.count(connection, sql, slug)
                    : Jdbc.count(connection, sql, slug, excludeId);

            return count > 0;
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to check the broadcast URL.", exception);
        }
    }

    public long create(Post post) {
        try (Connection connection = Db.connection()) {
            return Jdbc.insert(connection, """
                    INSERT INTO posts (user_id, category_id, type, status, title, slug, body,
                        cover_image_path, is_pinned, published_at, event_starts_at, event_ends_at,
                        event_location, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                        now() AT TIME ZONE 'Asia/Jakarta', now() AT TIME ZONE 'Asia/Jakarta')
                    """,
                    post.getUserId(), post.getCategoryId(), post.getType().getValue(),
                    post.getStatus().getValue(), post.getTitle(), post.getSlug(), post.getBody(),
                    post.getCoverImagePath(), post.isPinned(), post.getPublishedAt(),
                    post.getEventStartsAt(), post.getEventEndsAt(), post.getEventLocation());
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to save the broadcast.", exception);
        }
    }

    public void update(Post post) {
        try (Connection connection = Db.connection()) {
            Jdbc.update(connection, """
                    UPDATE posts SET category_id = ?, type = ?, status = ?, title = ?, slug = ?,
                        body = ?, cover_image_path = ?, published_at = ?, event_starts_at = ?,
                        event_ends_at = ?, event_location = ?,
                        updated_at = (now() AT TIME ZONE 'Asia/Jakarta')
                    WHERE id = ?
                    """,
                    post.getCategoryId(), post.getType().getValue(), post.getStatus().getValue(),
                    post.getTitle(), post.getSlug(), post.getBody(), post.getCoverImagePath(),
                    post.getPublishedAt(), post.getEventStartsAt(), post.getEventEndsAt(),
                    post.getEventLocation(), post.getId());
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to update the broadcast.", exception);
        }
    }

    public void delete(long id) {
        try (Connection connection = Db.connection()) {
            Jdbc.update(connection, "DELETE FROM posts WHERE id = ?", id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to delete the broadcast.", exception);
        }
    }

    public List<Post> latestForUser(long userId) {
        String sql = SELECT + " WHERE p.user_id = ? ORDER BY p.created_at DESC, p.id DESC";

        try (Connection connection = Db.connection()) {
            return Jdbc.list(connection, sql, PostDao::map, userId);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load your broadcasts.", exception);
        }
    }

    public List<String> coverPathsForUser(long userId) {
        try (Connection connection = Db.connection()) {
            return Jdbc.list(connection,
                    "SELECT cover_image_path FROM posts WHERE user_id = ? AND cover_image_path IS NOT NULL",
                    resultSet -> resultSet.getString("cover_image_path"), userId);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load cover images.", exception);
        }
    }

    public long countForUser(long userId) {
        try (Connection connection = Db.connection()) {
            return Jdbc.count(connection, "SELECT count(*) FROM posts WHERE user_id = ?", userId);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to count broadcasts.", exception);
        }
    }

    public long countForUserWithStatus(long userId, PostStatus status) {
        try (Connection connection = Db.connection()) {
            return Jdbc.count(connection,
                    "SELECT count(*) FROM posts WHERE user_id = ? AND status = ?",
                    userId, status.getValue());
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to count broadcasts.", exception);
        }
    }

    private static Post map(ResultSet resultSet) throws SQLException {
        Post post = new Post();
        post.setId(resultSet.getLong("id"));
        post.setUserId(resultSet.getLong("user_id"));
        post.setCategoryId(resultSet.getLong("category_id"));
        post.setType(PostType.fromValue(resultSet.getString("type")));
        post.setStatus(PostStatus.fromValue(resultSet.getString("status")));
        post.setTitle(resultSet.getString("title"));
        post.setSlug(resultSet.getString("slug"));
        post.setBody(resultSet.getString("body"));
        post.setCoverImagePath(resultSet.getString("cover_image_path"));
        post.setPinned(resultSet.getBoolean("is_pinned"));
        post.setPublishedAt(Jdbc.dateTime(resultSet, "published_at"));
        post.setEventStartsAt(Jdbc.dateTime(resultSet, "event_starts_at"));
        post.setEventEndsAt(Jdbc.dateTime(resultSet, "event_ends_at"));
        post.setEventLocation(resultSet.getString("event_location"));
        post.setCreatedAt(Jdbc.dateTime(resultSet, "created_at"));
        post.setUpdatedAt(Jdbc.dateTime(resultSet, "updated_at"));

        User author = new User();
        author.setId(resultSet.getLong("user_id"));
        author.setName(resultSet.getString("author_name"));
        author.setEmail(resultSet.getString("author_email"));
        author.setAvatarPath(resultSet.getString("author_avatar"));
        author.setModerator(resultSet.getBoolean("author_moderator"));
        post.setAuthor(author);

        Category category = new Category();
        category.setId(resultSet.getLong("category_id"));
        category.setName(resultSet.getString("category_name"));
        category.setSlug(resultSet.getString("category_slug"));
        post.setCategory(category);

        post.setCommentCount(resultSet.getLong("comment_count"));
        post.setBookmarkCount(resultSet.getLong("bookmark_count"));

        try {
            post.setBookmarked(resultSet.getBoolean("bookmarked"));
        } catch (SQLException ignored) {
            post.setBookmarked(false);
        }

        return post;
    }
}
