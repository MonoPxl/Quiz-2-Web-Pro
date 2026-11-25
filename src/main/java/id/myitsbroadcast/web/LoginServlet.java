package id.myitsbroadcast.web;

import id.myitsbroadcast.model.User;
import id.myitsbroadcast.service.AuthService;
import id.myitsbroadcast.util.Auth;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Flash;
import id.myitsbroadcast.util.Throttle;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Locale;

/** Email/password login with the source application's five-attempt throttle. */
@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    private final AuthService auth = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Log in — myITS Broadcast");
        request.setAttribute("layout", "auth");
        request.setAttribute("email", "");
        request.setAttribute("errors", new Errors());

        render(request, response, "auth/login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = param(request, "email");
        String password = request.getParameter("password");
        boolean remember = request.getParameter("remember") != null;
        Errors errors = new Errors();
        String throttleKey = email.toLowerCase(Locale.ROOT) + "|" + request.getRemoteAddr();

        if (email.isBlank()) {
            errors.add("email", "The email field is required.");
        }

        if (password == null || password.isEmpty()) {
            errors.add("password", "The password field is required.");
        }

        if (!errors.any()) {
            if (Throttle.tooMany(throttleKey)) {
                errors.add("email", "Too many login attempts. Please try again in a minute.");
            } else {
                User user = auth.authenticate(email, password);

                if (user == null) {
                    Throttle.hit(throttleKey);
                    errors.add("email", "These credentials do not match our records.");
                } else {
                    Throttle.clear(throttleKey);
                    String intended = intendedUrl(request);
                    Auth.login(request, response, user, remember);
                    Flash.success(request.getSession(), "Welcome back, " + user.getFirstName() + ".");
                    redirect(request, response, intended == null ? "/dashboard" : intended);
                    return;
                }
            }
        }

        request.setAttribute("pageTitle", "Log in — myITS Broadcast");
        request.setAttribute("layout", "auth");
        request.setAttribute("email", email);
        request.setAttribute("errors", errors);

        render(request, response, "auth/login", 422);
    }

    /** Read and clear the post-login destination before the session is rotated. */
    private static String intendedUrl(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object stored = session.getAttribute("intended_url");
        session.removeAttribute("intended_url");

        if (!(stored instanceof String intended) || !intended.startsWith("/") || intended.startsWith("//")) {
            return null;
        }

        return intended;
    }
}
