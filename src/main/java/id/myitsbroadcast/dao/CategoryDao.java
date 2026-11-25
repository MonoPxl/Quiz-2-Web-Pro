package id.myitsbroadcast.dao;

import id.myitsbroadcast.config.Db;
import id.myitsbroadcast.model.Category;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CategoryDao {

    public List<Category> all() {
        try (Connection connection = Db.connection()) {
            return Jdbc.list(connection,
                    "SELECT id, name, slug, description, created_at, updated_at "
                            + "FROM categories ORDER BY name ASC",
                    CategoryDao::map);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load categories.", exception);
        }
    }

    public Category findBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }

        try (Connection connection = Db.connection()) {
            return Jdbc.one(connection,
                    "SELECT id, name, slug, description, created_at, updated_at "
                            + "FROM categories WHERE slug = ?",
                    CategoryDao::map, slug);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load the category.", exception);
        }
    }

    public Category findById(long id) {
        try (Connection connection = Db.connection()) {
            return Jdbc.one(connection,
                    "SELECT id, name, slug, description, created_at, updated_at "
                            + "FROM categories WHERE id = ?",
                    CategoryDao::map, id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load the category.", exception);
        }
    }

    public boolean exists(long id) {
        try (Connection connection = Db.connection()) {
            return Jdbc.count(connection, "SELECT count(*) FROM categories WHERE id = ?", id) > 0;
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to check the category.", exception);
        }
    }

    private static Category map(ResultSet resultSet) throws SQLException {
        Category category = new Category();
        category.setId(resultSet.getLong("id"));
        category.setName(resultSet.getString("name"));
        category.setSlug(resultSet.getString("slug"));
        category.setDescription(resultSet.getString("description"));
        category.setCreatedAt(Jdbc.dateTime(resultSet, "created_at"));
        category.setUpdatedAt(Jdbc.dateTime(resultSet, "updated_at"));

        return category;
    }
}
