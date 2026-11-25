package id.myitsbroadcast.web;

import id.myitsbroadcast.model.Post;
import id.myitsbroadcast.model.User;
import id.myitsbroadcast.service.PostService;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Flash;
import id.myitsbroadcast.util.Uploads;
import id.myitsbroadcast.web.forms.PostForm;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.List;

/** Post detail, create, edit and delete. */
@WebServlet("/posts/*")
@MultipartConfig(maxFileSize = 2L * 1024 * 1024, maxRequestSize = 3L * 1024 * 1024)
public class PostServlet extends BaseServlet {

    private final PostService posts = new PostService();
    private final id.myitsbroadcast.service.CommentService comments =
            new id.myitsbroadcast.service.CommentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String info = request.getPathInfo() == null ? "/" : request.getPathInfo();

        if (info.equals("/") || info.isBlank()) {
            redirect(request, response, "/");
            return;
        }

        if (info.equals("/create")) {
            request.setAttribute("pageTitle", "Create a broadcast — myITS Broadcast");
            request.setAttribute("currentNav", "create");
            showForm(request, response, null, new PostForm(), new Errors(), false);
            return;
        }

        String[] segments = info.substring(1).split("/");

        if (segments.length == 1) {
            Post post = posts.findVisible(segments[0], currentUser(request));

            if (post == null) {
                notFound(request, response);
                return;
            }

            request.setAttribute("pageTitle", post.getTitle() + " — myITS Broadcast");
            request.setAttribute("currentNav", "feed");

            id.myitsbroadcast.model.Comment editing = editingComment(request, post);

            if (editing != null) {
                request.setAttribute("editingCommentId", editing.getId());
                showPost(request, response, post, null, editing.getBody(), null);
                return;
            }

            showPost(request, response, post);
            return;
        }

        if (segments.length == 2 && segments[1].equals("edit")) {
            Post post = posts.findVisible(segments[0], currentUser(request));

            if (post == null) {
                notFound(request, response);
                return;
            }

            if (!posts.canEdit(currentUser(request), post)) {
                forbidden(request, response);
                return;
            }

            request.setAttribute("pageTitle", "Edit broadcast — myITS Broadcast");
            request.setAttribute("currentNav", "dashboard");
            showForm(request, response, post, formFrom(post), new Errors(), true);
            return;
        }

        notFound(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String info = request.getPathInfo() == null ? "/" : request.getPathInfo();

        if (info.equals("/create")) {
            create(request, response);
            return;
        }

        String[] segments = info.substring(1).split("/");

        if (segments.length != 1) {
            notFound(request, response);
            return;
        }

        String method = param(request, "_method").toUpperCase();
        Post post = posts.findVisible(segments[0], currentUser(request));

        if (post == null) {
            notFound(request, response);
            return;
        }

        if (!posts.canEdit(currentUser(request), post)) {
            forbidden(request, response);
            return;
        }

        switch (method) {
            case "PUT", "PATCH" -> update(request, response, post);
            case "DELETE" -> delete(request, response, post);
            default -> response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PostForm form = readForm(request);
        Part cover = coverPart(request);
        Errors errors = posts.validate(form, cover);

        if (errors.any()) {
            request.setAttribute("pageTitle", "Create a broadcast — myITS Broadcast");
            request.setAttribute("currentNav", "create");
            showForm(request, response, null, form, errors, false, 422);
            return;
        }

        try {
            Post post = posts.create(currentUser(request), form, cover);
            Flash.success(request.getSession(),
                    post.isPublished() ? "Post published successfully." : "Draft saved successfully.");

            redirect(request, response,
                    post.isPublished() ? "/posts/" + post.getSlug() : "/dashboard");
        } catch (Uploads.InvalidUploadException exception) {
            errors.add("coverImage", exception.getMessage());
            request.setAttribute("pageTitle", "Create a broadcast — myITS Broadcast");
            showForm(request, response, null, form, errors, false, 422);
        }
    }

    private void update(HttpServletRequest request, HttpServletResponse response, Post post)
            throws ServletException, IOException {
        PostForm form = readForm(request);
        Part cover = coverPart(request);
        Errors errors = posts.validate(form, cover);

        if (errors.any()) {
            request.setAttribute("pageTitle", "Edit broadcast — myITS Broadcast");
            request.setAttribute("currentNav", "dashboard");
            showForm(request, response, post, form, errors, true, 422);
            return;
        }

        try {
            posts.update(post, form, cover);
            Flash.success(request.getSession(), "Post updated successfully.");
            redirect(request, response, "/posts/" + post.getSlug());
        } catch (Uploads.InvalidUploadException exception) {
            errors.add("coverImage", exception.getMessage());
            request.setAttribute("pageTitle", "Edit broadcast — myITS Broadcast");
            showForm(request, response, post, form, errors, true, 422);
        }
    }

    private void delete(HttpServletRequest request, HttpServletResponse response, Post post)
            throws IOException {
        posts.delete(post);
        Flash.success(request.getSession(), "Post deleted successfully.");
        redirect(request, response, "/dashboard");
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Post post, PostForm form, Errors errors, boolean edit)
            throws ServletException, IOException {
        showForm(request, response, post, form, errors, edit, 200);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Post post,
                          PostForm form, Errors errors, boolean edit, int status)
            throws ServletException, IOException {
        request.setAttribute("post", post);
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("isEdit", edit);
        request.setAttribute("categories", posts.categories());
        request.setAttribute("eventTypes", List.of("announcement", "event"));

        render(request, response, "posts/form", status);
    }

    private PostForm readForm(HttpServletRequest request) {
        PostForm form = new PostForm();
        form.setTitle(param(request, "title"));
        form.setCategoryId(param(request, "category_id"));
        form.setType(param(request, "type"));
        form.setBody(param(request, "body"));
        form.setStatus(param(request, "status"));
        form.setEventStartsAt(param(request, "event_starts_at"));
        form.setEventEndsAt(param(request, "event_ends_at"));
        form.setEventLocation(param(request, "event_location"));
        form.setRemoveCoverImage(request.getParameter("remove_cover_image") != null);

        return form;
    }

    private Part coverPart(HttpServletRequest request) {
        try {
            return request.getPart("cover_image");
        } catch (IOException | ServletException exception) {
            return null;
        }
    }

    /** Load a comment the viewer owns so the page can open inline edit mode. */
    private id.myitsbroadcast.model.Comment editingComment(HttpServletRequest request, Post post) {
        String value = param(request, "edit");

        if (value.isBlank()) {
            return null;
        }

        try {
            id.myitsbroadcast.model.Comment comment = comments.find(Long.parseLong(value));

            return comment != null
                    && comment.getPostId() == post.getId()
                    && comment.isOwnedBy(currentUser(request))
                    ? comment
                    : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private PostForm formFrom(Post post) {
        PostForm form = new PostForm();
        form.setTitle(post.getTitle());
        form.setCategoryId(Long.toString(post.getCategoryId()));
        form.setType(post.getType() == null ? "announcement" : post.getType().getValue());
        form.setBody(post.getBody());
        form.setStatus(post.getStatus() == null ? "draft" : post.getStatus().getValue());
        form.setEventStartsAt(new id.myitsbroadcast.util.Dates().input(post.getEventStartsAt()));
        form.setEventEndsAt(new id.myitsbroadcast.util.Dates().input(post.getEventEndsAt()));
        form.setEventLocation(post.getEventLocation() == null ? "" : post.getEventLocation());

        return form;
    }
}
