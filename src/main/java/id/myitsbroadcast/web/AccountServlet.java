package id.myitsbroadcast.web;

import id.myitsbroadcast.model.User;
import id.myitsbroadcast.service.ProfileService;
import id.myitsbroadcast.util.Auth;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Account deletion, guarded by the current password. */
@WebServlet("/settings/account")
public class AccountServlet extends BaseServlet {

    private final ProfileService profiles = new ProfileService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        Errors errors = profiles.deleteAccount(user, request.getParameter("password"));

        if (errors.any()) {
            request.setAttribute("pageTitle", "Profile settings — myITS Broadcast");
            request.setAttribute("layout", "settings");
            request.setAttribute("currentNav", "profile");
            request.setAttribute("formName", user.getName());
            request.setAttribute("formEmail", user.getEmail());
            request.setAttribute("errors", new Errors());
            request.setAttribute("deleteErrors", errors);
            render(request, response, "settings/profile", 422);
            return;
        }

        Auth.logout(request, response);
        request.getSession(true);
        Flash.success(request.getSession(), "Your account has been deleted.");
        redirect(request, response, "/");
    }
}
