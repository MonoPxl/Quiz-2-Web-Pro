package id.myitsbroadcast.service;

import id.myitsbroadcast.dao.CommentDao;
import id.myitsbroadcast.model.Comment;
import id.myitsbroadcast.model.User;
import id.myitsbroadcast.util.Errors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Service-level tests for the one approved product addition: editing one's own
 * comment or reply. The DAO is replaced with a recorder so the tests exercise
 * authorization and validation without a PostgreSQL server.
 */
class CommentServiceTest {

    private final RecordingCommentDao dao = new RecordingCommentDao();
    private final CommentService service = new CommentService(dao);

    @Test
    void authorCanEditOwnComment() {
        Comment comment = comment(10, 7, null);

        assertTrue(service.update(user(7), comment, "  Updated body  "));
        assertEquals(10, dao.updatedId);
        assertEquals("Updated body", dao.updatedBody);
    }

    @Test
    void authorCanEditOwnReply() {
        Comment reply = comment(11, 7, 10L);

        assertTrue(service.canEdit(user(7), reply));
        assertTrue(service.update(user(7), reply, "Updated reply"));
        assertEquals("Updated reply", dao.updatedBody);
        assertEquals(10L, reply.getParentId(), "editing must not change reply parentage");
    }

    @Test
    void anotherUserCannotEditTheComment() {
        Comment comment = comment(10, 7, null);

        assertFalse(service.canEdit(user(8), comment));
        assertFalse(service.update(user(8), comment, "Hijacked"));
        assertNull(dao.updatedId);
        assertNull(dao.updatedBody);
    }

    @Test
    void moderatorGainsNoEditRights() {
        User moderator = user(99);
        moderator.setModerator(true);
        Comment comment = comment(10, 7, null);

        assertFalse(service.update(moderator, comment, "Moderated"));
        assertNull(dao.updatedId);
    }

    @Test
    void unauthenticatedUpdateIsRefused() {
        Comment comment = comment(10, 7, null);

        assertFalse(service.update(null, comment, "Anonymous"));
        assertFalse(service.canEdit(null, comment));
        assertNull(dao.updatedId);
    }

    @Test
    void missingCommentIsRefused() {
        assertFalse(service.update(user(7), null, "Text"));
        assertFalse(service.canEdit(user(7), null));
    }

    @Test
    void deleteKeepsOwnershipRules() {
        Comment comment = comment(10, 7, null);

        assertFalse(service.delete(user(8), comment));
        assertEquals(0, dao.deletedId);

        assertTrue(service.delete(user(7), comment));
        assertEquals(10, dao.deletedId);
    }

    @Test
    void rejectsEmptyCommentBody() {
        Errors errors = service.validate("   ");

        assertTrue(errors.has("body"));
        assertEquals("The comment field is required.", errors.get("body"));
    }

    @Test
    void rejectsNullAndOverlongBodies() {
        assertTrue(service.validate(null).has("body"));
        assertTrue(service.validate("x".repeat(CommentService.MAX_BODY + 1)).has("body"));
    }

    @Test
    void acceptsBodyAtTheLimit() {
        assertFalse(service.validate("x".repeat(CommentService.MAX_BODY)).any());
        assertFalse(service.validate("A useful reply.").any());
    }

    private static User user(long id) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail("user" + id + "@student.its.ac.id");

        return user;
    }

    private static Comment comment(long id, long userId, Long parentId) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setPostId(3);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setBody("Original body");

        return comment;
    }

    private static final class RecordingCommentDao extends CommentDao {
        private Long updatedId;
        private String updatedBody;
        private long deletedId;

        @Override
        public void updateBody(long id, String body) {
            this.updatedId = id;
            this.updatedBody = body;
        }

        @Override
        public void delete(long id) {
            this.deletedId = id;
        }
    }
}
