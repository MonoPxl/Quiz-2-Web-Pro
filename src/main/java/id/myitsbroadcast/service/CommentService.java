package id.myitsbroadcast.service;

import id.myitsbroadcast.dao.CommentDao;
import id.myitsbroadcast.model.Comment;
import id.myitsbroadcast.model.Post;
import id.myitsbroadcast.model.User;
import id.myitsbroadcast.util.Errors;

import java.util.List;

public class CommentService {

    public static final int MAX_BODY = 2000;

    private final CommentDao comments;

    public CommentService() {
        this(new CommentDao());
    }

    /** Seam for focused unit tests that must not open a database connection. */
    CommentService(CommentDao comments) {
        this.comments = comments;
    }

    public List<Comment> thread(Post post) {
        return post.isPublished() ? comments.threadForPost(post.getId()) : List.of();
    }

    public Errors validate(String body) {
        Errors errors = new Errors();
        String trimmed = body == null ? "" : body.trim();

        if (trimmed.isEmpty()) {
            errors.add("body", "The comment field is required.");
        } else if (trimmed.length() > MAX_BODY) {
            errors.add("body", "The comment must not be longer than 2,000 characters.");
        }

        return errors;
    }

    /**
     * Create a comment or reply. Replies are always attached to the top-level
     * comment so the discussion never grows past one nested level.
     */
    public void create(User author, Post post, String body, String parentIdValue) {
        Long parentId = null;

        if (parentIdValue != null && !parentIdValue.isBlank()) {
            long id;

            try {
                id = Long.parseLong(parentIdValue);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("The reply target is invalid.");
            }

            Comment parent = comments.findById(id);

            if (parent == null || parent.getPostId() != post.getId()) {
                throw new IllegalArgumentException("The reply target does not belong to this broadcast.");
            }

            parentId = parent.getParentId() == null ? parent.getId() : parent.getParentId();
        }

        comments.create(post.getId(), author.getId(), parentId, body.trim());
    }

    public Comment find(long id) {
        return comments.findById(id);
    }

    /**
     * Only the author may edit or delete a comment or reply. Moderators gain no
     * edit rights here because the legacy application did not grant them.
     */
    public boolean canEdit(User user, Comment comment) {
        return comment != null && comment.isOwnedBy(user);
    }

    /** Update only the body; ownership is enforced here and by the servlet. */
    public boolean update(User author, Comment comment, String body) {
        if (!canEdit(author, comment)) {
            return false;
        }

        comments.updateBody(comment.getId(), body.trim());

        return true;
    }

    public boolean delete(User author, Comment comment) {
        if (!canEdit(author, comment)) {
            return false;
        }

        comments.delete(comment.getId());

        return true;
    }
}
