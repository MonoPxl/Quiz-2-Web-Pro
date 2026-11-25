package id.myitsbroadcast.web;

import id.myitsbroadcast.model.User;
import id.myitsbroadcast.service.ProfileService;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Password change, protected by the recent-password-confirmation gate. */
@WebServlet("/settings/security")
public class SecurityServlet extends BaseServlet {

    private final ProfileService profiles = new ProfileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ConfirmPasswordServlet.recentlyConfirmed(request)) {
            request.getSession().setAttribute("confirm_intended", "/settings/security");
            redirect(request, response, "/confirm-password");
            return;
        }

        show(request, response, new Errors(), 200);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ConfirmPasswordServlet.recentlyConfirmed(request)) {
            request.getSession().setAttribute("confirm_intended", "/settings/security");
            redirect(request, response, "/confirm-password");
            return;
        }

        User user = currentUser(request);
        Errors errors = profiles.updatePassword(user,
                request.getParameter("current_password"),
                request.getParameter("password"),
                request.getParameter("password_confirmation"));

        if (errors.any()) {
            show(request, response, errors, 422);
            return;
        }

        Flash.success(request.getSession(), "Password updated.");
        redirect(request, response, "/settings/security");
    }

    private void show(HttpServletRequest request, HttpServletResponse response, Errors errors, int status)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Security settings — myITS Broadcast");
        request.setAttribute("layout", "settings");
        request.setAttribute("currentNav", "profile");
        request.setAttribute("errors", errors);

        render(request, response, "settings/security", status);
    }
}
