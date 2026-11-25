package id.myitsbroadcast.web;

import id.myitsbroadcast.model.Post;
import id.myitsbroadcast.service.BookmarkService;
import id.myitsbroadcast.service.CommentService;
import id.myitsbroadcast.util.Auth;
import id.myitsbroadcast.util.Csrf;
import id.myitsbroadcast.util.Dates;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Flash;
import id.myitsbroadcast.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Shared request handling: view rendering, redirects, flash messages and the
 * small set of attributes every page needs.
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final String VIEW_ROOT = "/WEB-INF/views/";

    protected void render(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        prepare(request);
        request.getRequestDispatcher(VIEW_ROOT + view + ".jsp").forward(request, response);
    }

    protected void render(HttpServletRequest request, HttpServletResponse response, String view, int status)
            throws ServletException, IOException {
        response.setStatus(status);
        render(request, response, view);
    }

    protected void prepare(HttpServletRequest request) {
        request.setAttribute("csrfToken", Csrf.token(request));
        request.setAttribute("dates", new Dates());
        request.setAttribute("currentUser", Auth.user(request));
        request.setAttribute("currentPath", path(request));

        if (request.getAttribute("statusMessage") == null) {
            Flash.consume(request);
        }
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String location)
            throws IOException {
        response.sendRedirect(request.getContextPath() + location);
    }

    protected static String path(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();

        return context.isEmpty() ? uri : uri.substring(context.length());
    }

    protected static User currentUser(HttpServletRequest request) {
        return (User) request.getAttribute("currentUser");
    }

    protected static String param(HttpServletRequest request, String name) {
        String value = request.getParameter(name);

        return value == null ? "" : value.trim();
    }

    protected static int intParam(HttpServletRequest request, String name, int fallback) {
        try {
            return Integer.parseInt(param(request, name));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    protected static boolean wantsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");

        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
                || (accept != null && accept.contains("application/json"));
    }

    protected static void json(HttpServletResponse response, int status, Map<String, ?> payload)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(toJson(payload));
    }

    /** Minimal JSON writer for the small bookmark response payload. */
    private static String toJson(Map<String, ?> payload) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, ?> entry : payload.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;

            json.append('"').append(escape(entry.getKey())).append("\":");

            Object value = entry.getValue();

            if (value == null) {
                json.append("null");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else {
                json.append('"').append(escape(value.toString())).append('"');
            }
        }

        return json.append('}').toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("<", "\\u003c")
                .replace(">", "\\u003e")
                .replace("&", "\\u0026");
    }

    protected void notFound(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        render(request, response, "errors/404");
    }

    protected void forbidden(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        render(request, response, "errors/403");
    }

    /**
     * Render the post detail and discussion. Shared by the post, comment and
     * bookmark controllers so validation failures keep the reader on the page
     * with their input intact.
     */
    protected void showPost(HttpServletRequest request, HttpServletResponse response, Post post,
                            Errors commentErrors, String commentBody, String replyTo)
            throws ServletException, IOException {
        User viewer = Auth.user(request);

        request.setAttribute("post", post);
        request.setAttribute("comments", new CommentService().thread(post));
        request.setAttribute("bookmarked", new BookmarkService().isBookmarked(viewer, post));
        request.setAttribute("canEditPost", viewer != null && post.getUserId() == viewer.getId());
        request.setAttribute("commentErrors", commentErrors == null ? new Errors() : commentErrors);
        request.setAttribute("commentBody", commentBody == null ? "" : commentBody);
        request.setAttribute("replyTo", replyTo);
        request.setAttribute("commentCount", post.getCommentCount());

        render(request, response, "posts/show");
    }

    protected void showPost(HttpServletRequest request, HttpServletResponse response, Post post)
            throws ServletException, IOException {
        showPost(request, response, post, null, null, null);
    }
}
