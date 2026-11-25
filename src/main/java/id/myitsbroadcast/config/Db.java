package id.myitsbroadcast.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Lazily initialised HikariCP connection pool.
 *
 * <p>The pool is created on first use so that the web application can start
 * and render a clear configuration error page even when PostgreSQL is not
 * reachable, instead of failing inside the container's deploy phase.</p>
 */
public final class Db {

    private static volatile HikariDataSource dataSource;

    private Db() {
    }

    public static HikariDataSource dataSource() {
        HikariDataSource current = dataSource;

        if (current == null) {
            synchronized (Db.class) {
                current = dataSource;

                if (current == null) {
                    HikariConfig config = new HikariConfig();
                    config.setDriverClassName("org.postgresql.Driver");
                    config.setJdbcUrl(AppConfig.databaseUrl());
                    config.setUsername(AppConfig.databaseUser());
                    config.setPassword(AppConfig.databasePassword());
                    config.setMaximumPoolSize(AppConfig.integer("DB_POOL_SIZE", 10));
                    config.setMinimumIdle(1);
                    config.setPoolName("myitsbroadcast-pool");
                    config.setConnectionTimeout(10_000);
                    config.setValidationTimeout(5_000);
                    config.setInitializationFailTimeout(-1);

                    current = new HikariDataSource(config);
                    dataSource = current;
                }
            }
        }

        return current;
    }

    public static Connection connection() throws SQLException {
        return dataSource().getConnection();
    }

    public static void close() {
        HikariDataSource current = dataSource;

        if (current != null) {
            current.close();
            dataSource = null;
        }
    }
}
