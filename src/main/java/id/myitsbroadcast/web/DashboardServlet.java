package id.myitsbroadcast.web;

import id.myitsbroadcast.service.DashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** The signed-in user's own posts, counts and saved broadcasts. */
@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {

    private final DashboardService dashboard = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "My Broadcast — myITS Broadcast");
        request.setAttribute("currentNav", "dashboard");
        request.setAttribute("dashboard", dashboard.forUser(currentUser(request)));

        render(request, response, "dashboard");
    }
}
