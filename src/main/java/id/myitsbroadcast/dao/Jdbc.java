package id.myitsbroadcast.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Small JDBC helper used by every DAO.
 *
 * <p>All values are bound with {@link PreparedStatement}; no user input is ever
 * concatenated into SQL.</p>
 */
final class Jdbc {

    private Jdbc() {
    }

    interface RowMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }

    static <T> List<T> list(Connection connection, String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        List<T> results = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapper.map(resultSet));
                }
            }
        }

        return results;
    }

    static <T> T one(Connection connection, String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        List<T> results = list(connection, sql, mapper, params);

        return results.isEmpty() ? null : results.get(0);
    }

    static long count(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : 0L;
            }
        }
    }

    static int update(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);

            return statement.executeUpdate();
        }
    }

    static long insert(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, params);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        }
    }

    static void bind(PreparedStatement statement, Object... params) throws SQLException {
        for (int index = 0; index < params.length; index++) {
            Object value = params[index];
            int position = index + 1;

            if (value == null) {
                statement.setObject(position, null);
            } else if (value instanceof LocalDateTime dateTime) {
                statement.setTimestamp(position, Timestamp.valueOf(dateTime));
            } else {
                statement.setObject(position, value);
            }
        }
    }

    static LocalDateTime dateTime(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);

        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
