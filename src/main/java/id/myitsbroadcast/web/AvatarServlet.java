package id.myitsbroadcast.web;

import id.myitsbroadcast.service.ProfileService;
import id.myitsbroadcast.util.Flash;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Remove the signed-in user's profile photo. */
@WebServlet("/settings/profile/avatar")
public class AvatarServlet extends HttpServlet {

    private final ProfileService profiles = new ProfileService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        profiles.removeAvatar(id.myitsbroadcast.util.Auth.user(request));
        Flash.success(request.getSession(), "Profile photo removed.");
        response.sendRedirect(request.getContextPath() + "/settings/profile");
    }
}
