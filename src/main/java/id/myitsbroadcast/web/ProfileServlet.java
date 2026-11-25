package id.myitsbroadcast.web;

import id.myitsbroadcast.model.User;
import id.myitsbroadcast.service.ProfileService;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Flash;
import id.myitsbroadcast.util.Uploads;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;

/** Profile settings: name, email and profile photo. */
@WebServlet("/settings/profile")
@MultipartConfig(maxFileSize = 2L * 1024 * 1024, maxRequestSize = 3L * 1024 * 1024)
public class ProfileServlet extends BaseServlet {

    private final ProfileService profiles = new ProfileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        show(request, response, currentUser(request).getName(), currentUser(request).getEmail(),
                new Errors(), new Errors(), 200);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String name = param(request, "name");
        String email = param(request, "email");
        Part photo = photo(request);
        Errors errors = profiles.validateProfile(user, name, email);

        try {
            Uploads.validate(photo);
        } catch (Uploads.InvalidUploadException exception) {
            errors.add("photo", exception.getMessage());
        }

        if (errors.any()) {
            show(request, response, name, email, errors, new Errors(), 422);
            return;
        }

        try {
            profiles.updateProfile(user, name, email, photo);
            Flash.success(request.getSession(), "Profile updated.");
            redirect(request, response, "/settings/profile");
        } catch (Uploads.InvalidUploadException exception) {
            errors.add("photo", exception.getMessage());
            show(request, response, name, email, errors, new Errors(), 422);
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response, String name, String email,
                      Errors errors, Errors deleteErrors, int status) throws ServletException, IOException {
        request.setAttribute("pageTitle", "Profile settings — myITS Broadcast");
        request.setAttribute("layout", "settings");
        request.setAttribute("currentNav", "profile");
        request.setAttribute("formName", name);
        request.setAttribute("formEmail", email);
        request.setAttribute("errors", errors);
        request.setAttribute("deleteErrors", deleteErrors);

        render(request, response, "settings/profile", status);
    }

    private Part photo(HttpServletRequest request) {
        try {
            return request.getPart("photo");
        } catch (IOException | ServletException exception) {
            return null;
        }
    }
}
