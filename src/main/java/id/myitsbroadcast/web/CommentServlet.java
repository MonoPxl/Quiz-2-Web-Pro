package id.myitsbroadcast.web;

import id.myitsbroadcast.model.Comment;
import id.myitsbroadcast.model.Post;
import id.myitsbroadcast.model.User;
import id.myitsbroadcast.service.CommentService;
import id.myitsbroadcast.service.PostService;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Comment and reply creation, plus the author-only edit and delete actions. */
@WebServlet(urlPatterns = {"/comments", "/comments/*"})
public class CommentServlet extends BaseServlet {

    private final PostService posts = new PostService();
    private final CommentService comments = new CommentService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String info = request.getPathInfo();

        if (info == null || info.equals("/")) {
            create(request, response);
            return;
        }

        String[] segments = info.substring(1).split("/");

        if (segments.length != 1) {
            notFound(request, response);
            return;
        }

        long id;

        try {
            id = Long.parseLong(segments[0]);
        } catch (NumberFormatException exception) {
            notFound(request, response);
            return;
        }

        Comment comment = comments.find(id);

        if (comment == null) {
            notFound(request, response);
            return;
        }

        Post post = posts.findById(comment.getPostId());

        if (post == null) {
            notFound(request, response);
            return;
        }

        String method = param(request, "_method").toUpperCase();

        switch (method) {
            case "PUT", "PATCH" -> update(request, response, post, comment);
            case "DELETE" -> delete(request, response, post, comment);
            default -> response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        Post post = posts.findVisible(param(request, "post"), user);

        if (post == null || !post.isPublished()) {
            notFound(request, response);
            return;
        }

        String body = request.getParameter("body");
        String parentId = param(request, "parent_id");
        Errors errors = comments.validate(body);

        if (errors.any()) {
            request.setAttribute("pageTitle", post.getTitle() + " — myITS Broadcast");
            request.setAttribute("editingCommentId", null);
            response.setStatus(422);
            showPost(request, response, post, errors, body, parentId);
            return;
        }

        try {
            comments.create(user, post, body, parentId);
            Flash.success(request.getSession(), "Comment posted successfully.");
            redirect(request, response, "/posts/" + post.getSlug() + "#comments");
        } catch (IllegalArgumentException exception) {
            errors.add("body", exception.getMessage());
            showPost(request, response, post, errors, body, parentId);
        }
    }

    private void update(HttpServletRequest request, HttpServletResponse response, Post post, Comment comment)
            throws ServletException, IOException {
        User user = currentUser(request);

        if (!comment.isOwnedBy(user)) {
            forbidden(request, response);
            return;
        }

        String body = request.getParameter("body");
        Errors errors = comments.validate(body);

        if (errors.any()) {
            request.setAttribute("pageTitle", post.getTitle() + " — myITS Broadcast");
            request.setAttribute("editingCommentId", comment.getId());
            response.setStatus(422);
            showPost(request, response, post, errors, body, null);
            return;
        }

        comments.update(user, comment, body);
        Flash.success(request.getSession(), "Comment updated successfully.");
        redirect(request, response, "/posts/" + post.getSlug() + "#comment-" + comment.getId());
    }

    private void delete(HttpServletRequest request, HttpServletResponse response, Post post, Comment comment)
            throws IOException, ServletException {
        if (!comments.delete(currentUser(request), comment)) {
            forbidden(request, response);
            return;
        }

        Flash.success(request.getSession(), "Comment deleted successfully.");
        redirect(request, response, "/posts/" + post.getSlug() + "#comments");
    }
}
