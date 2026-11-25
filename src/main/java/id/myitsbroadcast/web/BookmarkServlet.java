package id.myitsbroadcast.web;

import id.myitsbroadcast.model.Post;
import id.myitsbroadcast.model.User;
import id.myitsbroadcast.service.BookmarkService;
import id.myitsbroadcast.service.PostService;
import id.myitsbroadcast.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Bookmark toggle. Browsers without JavaScript get a normal redirect; the
 * fetch-based UI receives JSON and updates in place without a reload.
 */
@WebServlet("/bookmarks")
public class BookmarkServlet extends BaseServlet {

    private final PostService posts = new PostService();
    private final BookmarkService bookmarks = new BookmarkService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        Post post = posts.findVisible(param(request, "post"), user);

        if (post == null || !post.isPublished()) {
            if (wantsJson(request)) {
                json(response, HttpServletResponse.SC_NOT_FOUND, Map.of("error", "not_found"));
                return;
            }

            notFound(request, response);
            return;
        }

        boolean bookmarked = bookmarks.toggle(user, post);
        String message = bookmarked ? "Post bookmarked." : "Bookmark removed.";

        if (wantsJson(request)) {
            json(response, HttpServletResponse.SC_OK, Map.of(
                    "bookmarked", bookmarked,
                    "message", message));
            return;
        }

        Flash.success(request.getSession(), message);
        redirect(request, response, "/posts/" + post.getSlug());
    }
}
