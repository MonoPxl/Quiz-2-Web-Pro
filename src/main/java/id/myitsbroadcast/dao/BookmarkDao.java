package id.myitsbroadcast.dao;

import id.myitsbroadcast.config.Db;
import id.myitsbroadcast.model.Bookmark;
import id.myitsbroadcast.model.Category;
import id.myitsbroadcast.model.Post;
import id.myitsbroadcast.model.PostStatus;
import id.myitsbroadcast.model.PostType;
import id.myitsbroadcast.model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class BookmarkDao {

    public boolean exists(long userId, long postId) {
        try (Connection connection = Db.connection()) {
            return Jdbc.count(connection,
                    "SELECT count(*) FROM bookmarks WHERE user_id = ? AND post_id = ?",
                    userId, postId) > 0;
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to check the bookmark.", exception);
        }
    }

    /** Idempotent insert; the unique index makes repeated clicks harmless. */
    public void add(long userId, long postId) {
        try (Connection connection = Db.connection()) {
            Jdbc.update(connection, """
                    INSERT INTO bookmarks (user_id, post_id, created_at, updated_at)
                    VALUES (?, ?, now() AT TIME ZONE 'Asia/Jakarta', now() AT TIME ZONE 'Asia/Jakarta')
                    ON CONFLICT (user_id, post_id) DO NOTHING
                    """, userId, postId);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to save the bookmark.", exception);
        }
    }

    public void remove(long userId, long postId) {
        try (Connection connection = Db.connection()) {
            Jdbc.update(connection, "DELETE FROM bookmarks WHERE user_id = ? AND post_id = ?", userId, postId);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to remove the bookmark.", exception);
        }
    }

    /** Saved posts that are still publicly visible, newest bookmark first. */
    public List<Bookmark> listForUser(long userId) {
        String sql = """
                SELECT b.id AS bookmark_id, b.created_at AS bookmarked_at,
                       p.id, p.user_id, p.category_id, p.type, p.status, p.title, p.slug, p.body,
                       p.cover_image_path, p.is_pinned, p.published_at, p.event_starts_at,
                       p.event_ends_at, p.event_location, p.created_at, p.updated_at,
                       u.name AS author_name, u.email AS author_email, u.avatar_path AS author_avatar,
                       u.is_moderator AS author_moderator,
                       c.name AS category_name, c.slug AS category_slug,
                       (SELECT count(*) FROM comments cm WHERE cm.post_id = p.id) AS comment_count
                FROM bookmarks b
                JOIN posts p ON p.id = b.post_id
                JOIN users u ON u.id = p.user_id
                JOIN categories c ON c.id = p.category_id
                WHERE b.user_id = ? AND p.status = 'published'
                  AND p.published_at IS NOT NULL AND p.published_at <= ?
                ORDER BY b.created_at DESC, b.id DESC
                """;

        try (Connection connection = Db.connection()) {
            return Jdbc.list(connection, sql, BookmarkDao::map, userId,
                    LocalDateTime.now(java.time.ZoneId.of(id.myitsbroadcast.config.AppConfig.APP_TIMEZONE)));
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load bookmarked broadcasts.", exception);
        }
    }

    public long countForUser(long userId) {
        try (Connection connection = Db.connection()) {
            return Jdbc.count(connection, """
                    SELECT count(*) FROM bookmarks b
                    JOIN posts p ON p.id = b.post_id
                    WHERE b.user_id = ? AND p.status = 'published' AND p.published_at IS NOT NULL
                      AND p.published_at <= ?
                    """, userId, LocalDateTime.now(java.time.ZoneId.of(
                    id.myitsbroadcast.config.AppConfig.APP_TIMEZONE)));
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to count bookmarks.", exception);
        }
    }

    private static Bookmark map(ResultSet resultSet) throws SQLException {
        Bookmark bookmark = new Bookmark();
        bookmark.setId(resultSet.getLong("bookmark_id"));
        bookmark.setCreatedAt(Jdbc.dateTime(resultSet, "bookmarked_at"));

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
        post.setCommentCount(resultSet.getLong("comment_count"));
        post.setBookmarked(true);

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

        bookmark.setPost(post);
        bookmark.setUserId(resultSet.getLong("user_id"));
        bookmark.setPostId(post.getId());

        return bookmark;
    }
}
