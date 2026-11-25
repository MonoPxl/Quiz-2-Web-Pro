package id.myitsbroadcast.web;

import id.myitsbroadcast.util.Auth;
import id.myitsbroadcast.util.Flash;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Logout endpoint. Deliberately absent from the shared navbar. */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Auth.logout(request, response);
        request.getSession(true);
        Flash.success(request.getSession(), "You have been logged out.");
        response.sendRedirect(request.getContextPath() + "/");
    }
}
