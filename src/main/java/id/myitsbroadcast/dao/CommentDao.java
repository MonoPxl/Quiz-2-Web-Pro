package id.myitsbroadcast.dao;

import id.myitsbroadcast.config.Db;
import id.myitsbroadcast.model.Comment;
import id.myitsbroadcast.model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommentDao {

    private static final String SELECT = """
            SELECT cm.id, cm.post_id, cm.user_id, cm.parent_id, cm.body, cm.created_at, cm.updated_at,
                   u.name AS author_name, u.email AS author_email, u.avatar_path AS author_avatar,
                   u.is_moderator AS author_moderator
            FROM comments cm
            JOIN users u ON u.id = cm.user_id
            """;

    /** Load the thread for a post in one query, then assemble one reply level in Java. */
    public List<Comment> threadForPost(long postId) {
        String sql = SELECT + " WHERE cm.post_id = ? ORDER BY cm.created_at ASC, cm.id ASC";

        try (Connection connection = Db.connection()) {
            List<Comment> all = Jdbc.list(connection, sql, CommentDao::map, postId);
            Map<Long, List<Comment>> repliesByParent = new LinkedHashMap<>();

            for (Comment comment : all) {
                if (comment.getParentId() != null) {
                    repliesByParent.computeIfAbsent(comment.getParentId(), ignored -> new ArrayList<>())
                            .add(comment);
                }
            }

            List<Comment> roots = new ArrayList<>();

            for (Comment comment : all) {
                if (comment.getParentId() == null) {
                    List<Comment> replies = repliesByParent.getOrDefault(comment.getId(), List.of());
                    comment.setReplies(replies);
                    comment.setReplyCount(replies.size());
                    roots.add(comment);
                }
            }

            return roots;
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load the discussion.", exception);
        }
    }

    public Comment findById(long id) {
        try (Connection connection = Db.connection()) {
            return Jdbc.one(connection, SELECT + " WHERE cm.id = ?", CommentDao::map, id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to load the comment.", exception);
        }
    }

    public long create(long postId, long userId, Long parentId, String body) {
        try (Connection connection = Db.connection()) {
            return Jdbc.insert(connection, """
                    INSERT INTO comments (post_id, user_id, parent_id, body, created_at, updated_at)
                    VALUES (?, ?, ?, ?, now() AT TIME ZONE 'Asia/Jakarta', now() AT TIME ZONE 'Asia/Jakarta')
                    """, postId, userId, parentId, body);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to post the comment.", exception);
        }
    }

    public void updateBody(long id, String body) {
        try (Connection connection = Db.connection()) {
            Jdbc.update(connection, """
                    UPDATE comments SET body = ?, updated_at = (now() AT TIME ZONE 'Asia/Jakarta')
                    WHERE id = ?
                    """, body, id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to update the comment.", exception);
        }
    }

    public void delete(long id) {
        try (Connection connection = Db.connection()) {
            Jdbc.update(connection, "DELETE FROM comments WHERE id = ?", id);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to delete the comment.", exception);
        }
    }

    public long countForPost(long postId) {
        try (Connection connection = Db.connection()) {
            return Jdbc.count(connection, "SELECT count(*) FROM comments WHERE post_id = ?", postId);
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to count comments.", exception);
        }
    }

    private static Comment map(ResultSet resultSet) throws SQLException {
        Comment comment = new Comment();
        comment.setId(resultSet.getLong("id"));
        comment.setPostId(resultSet.getLong("post_id"));
        comment.setUserId(resultSet.getLong("user_id"));
        long parentId = resultSet.getLong("parent_id");
        comment.setParentId(resultSet.wasNull() ? null : parentId);
        comment.setBody(resultSet.getString("body"));
        comment.setCreatedAt(Jdbc.dateTime(resultSet, "created_at"));
        comment.setUpdatedAt(Jdbc.dateTime(resultSet, "updated_at"));

        User author = new User();
        author.setId(resultSet.getLong("user_id"));
        author.setName(resultSet.getString("author_name"));
        author.setEmail(resultSet.getString("author_email"));
        author.setAvatarPath(resultSet.getString("author_avatar"));
        author.setModerator(resultSet.getBoolean("author_moderator"));
        comment.setUser(author);

        return comment;
    }
}
