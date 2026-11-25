package id.myitsbroadcast.dao;

import id.myitsbroadcast.config.Db;
import id.myitsbroadcast.model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public User findById(long id) {
        try (Connection connection = Db.connection()) {
            return Jdbc.one(connection, "SELECT * FROM users WHERE id = ?", UserDao::map, id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load the account.", exception);
        }
    }

    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        try (Connection connection = Db.connection()) {
            return Jdbc.one(connection,
                    "SELECT * FROM users WHERE lower(email) = lower(?)", UserDao::map, email.trim());
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load the account.", exception);
        }
    }

    public boolean emailExists(String email, Long excludeId) {
        String sql = "SELECT count(*) FROM users WHERE lower(email) = lower(?)"
                + (excludeId == null ? "" : " AND id <> ?");

        try (Connection connection = Db.connection()) {
            long count = excludeId == null
                    ? Jdbc.count(connection, sql, email)
                    : Jdbc.count(connection, sql, email, excludeId);

            return count > 0;
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to check the email address.", exception);
        }
    }

    public long create(String name, String email, String password, boolean moderator) {
        try (Connection connection = Db.connection()) {
            return Jdbc.insert(connection, """
                    INSERT INTO users (name, email, password, is_moderator, created_at, updated_at)
                    VALUES (?, ?, ?, ?, now() AT TIME ZONE 'Asia/Jakarta', now() AT TIME ZONE 'Asia/Jakarta')
                    """, name, email, password, moderator);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to create the account.", exception);
        }
    }

    public void updateProfile(long id, String name, String email) {
        try (Connection connection = Db.connection()) {
            Jdbc.update(connection, """
                    UPDATE users SET name = ?, email = ?,
                        updated_at = (now() AT TIME ZONE 'Asia/Jakarta')
                    WHERE id = ?
                    """, name, email, id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to update the profile.", exception);
        }
    }

    public void updateAvatar(long id, String avatarPath) {
        try (Connection connection = Db.connection()) {
            Jdbc.update(connection, """
                    UPDATE users SET avatar_path = ?,
                        updated_at = (now() AT TIME ZONE 'Asia/Jakarta')
                    WHERE id = ?
                    """, avatarPath, id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to update the profile photo.", exception);
        }
    }

    public void updatePassword(long id, String passwordHash) {
        try (Connection connection = Db.connection()) {
            Jdbc.update(connection, """
                    UPDATE users SET password = ?,
                        updated_at = (now() AT TIME ZONE 'Asia/Jakarta')
                    WHERE id = ?
                    """, passwordHash, id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to update the password.", exception);
        }
    }

    public void updateRememberToken(long id, String tokenHash) {
        try (Connection connection = Db.connection()) {
            Jdbc.update(connection, "UPDATE users SET remember_token = ? WHERE id = ?", tokenHash, id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to update the remember-me token.", exception);
        }
    }

    public void delete(long id) {
        try (Connection connection = Db.connection()) {
            Jdbc.update(connection, "DELETE FROM users WHERE id = ?", id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to delete the account.", exception);
        }
    }

    private static User map(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setRememberToken(resultSet.getString("remember_token"));
        user.setModerator(resultSet.getBoolean("is_moderator"));
        user.setAvatarPath(resultSet.getString("avatar_path"));
        user.setCreatedAt(Jdbc.dateTime(resultSet, "created_at"));
        user.setUpdatedAt(Jdbc.dateTime(resultSet, "updated_at"));

        return user;
    }
}
