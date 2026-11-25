package id.myitsbroadcast.web;

import id.myitsbroadcast.model.Category;
import id.myitsbroadcast.model.Page;
import id.myitsbroadcast.model.Post;
import id.myitsbroadcast.service.PostService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/** Public feed with keyword, category and type filtering plus pagination. */
@WebServlet(urlPatterns = {"", "/feed"})
public class FeedServlet extends BaseServlet {

    private final PostService posts = new PostService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = param(request, "q");
        String category = param(request, "category");
        String type = param(request, "type");
        int page = intParam(request, "page", 1);

        if (!category.isBlank() && posts.categoryBySlug(category) == null) {
            category = "";
        }

        if (!type.isBlank() && posts.typeFrom(type) == null) {
            type = "";
        }

        Page<Post> results = posts.feed(keyword, category.isBlank() ? null : category,
                type.isBlank() ? null : type, page, currentUser(request));
        List<Category> categories = posts.categories();
        boolean hasFilters = !keyword.isBlank() || !category.isBlank() || !type.isBlank();

        request.setAttribute("pageTitle", hasFilters
                ? "Search results — myITS Broadcast"
                : "Campus Bulletin — myITS Broadcast");
        request.setAttribute("layout", "home");
        request.setAttribute("currentNav", "feed");
        request.setAttribute("posts", results);
        request.setAttribute("categories", categories);
        request.setAttribute("q", keyword);
        request.setAttribute("selectedCategory", category);
        request.setAttribute("selectedType", type);
        request.setAttribute("hasFilters", hasFilters);

        render(request, response, "feed");
    }
}
