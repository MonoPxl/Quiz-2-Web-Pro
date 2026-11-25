package id.myitsbroadcast.web;

import id.myitsbroadcast.model.User;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Passwords;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Password confirmation gate in front of the security settings page. */
@WebServlet("/confirm-password")
public class ConfirmPasswordServlet extends BaseServlet {

    public static final String CONFIRMED_AT = "password_confirmed_at";
    public static final long CONFIRMATION_TTL_MILLIS = 3L * 60 * 60 * 1000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Confirm password — myITS Broadcast");
        request.setAttribute("layout", "auth");
        request.setAttribute("errors", new Errors());

        render(request, response, "auth/confirm-password");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String password = request.getParameter("password");
        Errors errors = new Errors();

        if (password == null || password.isEmpty()) {
            errors.add("password", "The password field is required.");
        } else if (!Passwords.check(password, user.getPassword())) {
            errors.add("password", "The provided password does not match your current password.");
        }

        if (errors.any()) {
            request.setAttribute("pageTitle", "Confirm password — myITS Broadcast");
            request.setAttribute("layout", "auth");
            request.setAttribute("errors", errors);
            render(request, response, "auth/confirm-password", 422);
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute(CONFIRMED_AT, System.currentTimeMillis());

        Object intended = session.getAttribute("confirm_intended");
        session.removeAttribute("confirm_intended");
        String target = intended instanceof String value && value.startsWith("/") && !value.startsWith("//")
                ? value
                : "/settings/security";

        redirect(request, response, target);
    }

    public static boolean recentlyConfirmed(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        Object confirmedAt = session.getAttribute(CONFIRMED_AT);

        return confirmedAt instanceof Long timestamp
                && System.currentTimeMillis() - timestamp < CONFIRMATION_TTL_MILLIS;
    }
}
