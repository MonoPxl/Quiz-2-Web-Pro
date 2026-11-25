package id.myitsbroadcast.config;

import java.nio.file.Path;

/**
 * Central access to runtime configuration.
 *
 * <p>Values come from environment variables first and JVM system properties
 * second, which keeps credentials out of the source tree and lets tests and
 * local runs override individual keys without editing files.</p>
 */
public final class AppConfig {

    public static final String APP_TIMEZONE = "Asia/Jakarta";

    private AppConfig() {
    }

    public static String get(String key, String fallback) {
        String value = System.getenv(key);

        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }

        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public static boolean flag(String key, boolean fallback) {
        String value = get(key, null);

        if (value == null) {
            return fallback;
        }

        return value.equalsIgnoreCase("true") || value.equals("1") || value.equalsIgnoreCase("yes");
    }

    public static int integer(String key, int fallback) {
        try {
            return Integer.parseInt(get(key, Integer.toString(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public static boolean production() {
        return "production".equalsIgnoreCase(get("APP_ENV", "development"));
    }

    public static String baseUrl() {
        return get("APP_BASE_URL", "http://localhost:8080").replaceAll("/+$", "");
    }

    public static Path storageDir() {
        return Path.of(get("APP_STORAGE_DIR",
                Path.of(System.getProperty("user.home"), "myitsbroadcast-storage").toString()));
    }

    /** JDBC URL for the application database, including any driver options. */
    public static String databaseUrl() {
        return get("DB_URL", "jdbc:postgresql://localhost:5432/myitsbroadcast");
    }

    public static String databaseUser() {
        return get("DB_USER", "postgres");
    }

    public static String databasePassword() {
        return get("DB_PASSWORD", "");
    }
}
