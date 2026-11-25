package id.myitsbroadcast.web;

import id.myitsbroadcast.model.User;
import id.myitsbroadcast.service.AuthService;
import id.myitsbroadcast.util.Auth;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Account registration. The account is usable immediately; there is no verification step. */
@WebServlet("/register")
public class RegisterServlet extends BaseServlet {

    private final AuthService auth = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Create an account — myITS Broadcast");
        request.setAttribute("layout", "auth");
        request.setAttribute("name", "");
        request.setAttribute("email", "");
        request.setAttribute("errors", new Errors());

        render(request, response, "auth/register");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = param(request, "name");
        String email = param(request, "email");
        String password = request.getParameter("password");
        String confirmation = request.getParameter("password_confirmation");
        Errors errors = auth.validateRegistration(name, email, password, confirmation);

        if (errors.any()) {
            request.setAttribute("pageTitle", "Create an account — myITS Broadcast");
            request.setAttribute("layout", "auth");
            request.setAttribute("name", name);
            request.setAttribute("email", email);
            request.setAttribute("errors", errors);
            render(request, response, "auth/register", 422);
            return;
        }

        User user = auth.register(name, email, password);
        Auth.login(request, response, user, false);
        Flash.success(request.getSession(), "Account created. Welcome to myITS Broadcast.");
        redirect(request, response, "/dashboard");
    }
}
