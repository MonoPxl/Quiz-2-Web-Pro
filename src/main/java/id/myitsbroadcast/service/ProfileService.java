package id.myitsbroadcast.service;

import id.myitsbroadcast.dao.PostDao;
import id.myitsbroadcast.dao.UserDao;
import id.myitsbroadcast.model.User;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Passwords;
import id.myitsbroadcast.util.Uploads;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/** Profile, photo, password and account-deletion rules. */
public class ProfileService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserDao users = new UserDao();
    private final PostDao posts = new PostDao();

    public Errors validateProfile(User user, String name, String email) {
        Errors errors = new Errors();
        String trimmedName = name == null ? "" : name.trim();
        String trimmedEmail = email == null ? "" : email.trim();

        if (trimmedName.isEmpty()) {
            errors.add("name", "The name field is required.");
        } else if (trimmedName.length() > 255) {
            errors.add("name", "The name must not be longer than 255 characters.");
        }

        if (trimmedEmail.isEmpty()) {
            errors.add("email", "The email field is required.");
        } else if (!EMAIL.matcher(trimmedEmail).matches()) {
            errors.add("email", "The email address must be a valid email address.");
        } else if (trimmedEmail.length() > 255) {
            errors.add("email", "The email must not be longer than 255 characters.");
        } else if (users.emailExists(trimmedEmail, user.getId())) {
            errors.add("email", "An account with this email already exists.");
        }

        return errors;
    }

    /**
     * Persist name/email and, when supplied, replace the avatar. The previous
     * file is removed only after the new path is stored.
     */
    public void updateProfile(User user, String name, String email, Part photo)
            throws Uploads.InvalidUploadException, IOException {
        users.updateProfile(user.getId(), name.trim(), email.trim());

        if (Uploads.isPresent(photo)) {
            String stored = Uploads.store(photo, "avatars");

            if (stored != null) {
                users.updateAvatar(user.getId(), stored);

                if (user.getAvatarPath() != null) {
                    Uploads.delete(user.getAvatarPath());
                }

                user.setAvatarPath(stored);
            }
        }

        user.setName(name.trim());
        user.setEmail(email.trim());
    }

    public void removeAvatar(User user) {
        if (user.getAvatarPath() != null) {
            Uploads.delete(user.getAvatarPath());
            users.updateAvatar(user.getId(), null);
            user.setAvatarPath(null);
        }
    }

    public Errors updatePassword(User user, String currentPassword, String password, String confirmation) {
        Errors errors = new Errors();

        if (currentPassword == null || currentPassword.isEmpty()) {
            errors.add("currentPassword", "The current password field is required.");
        } else if (!Passwords.check(currentPassword, user.getPassword())) {
            errors.add("currentPassword", "The provided password does not match your current password.");
        }

        for (String message : Passwords.validate(password, confirmation)) {
            errors.add("password", message);
        }

        if (errors.any()) {
            return errors;
        }

        String hash = Passwords.hash(password);
        users.updatePassword(user.getId(), hash);
        user.setPassword(hash);

        return errors;
    }

    /** Delete the account and every owned image file, mirroring the source model hooks. */
    public Errors deleteAccount(User user, String password) {
        Errors errors = new Errors();

        if (password == null || password.isEmpty()) {
            errors.add("password", "The password field is required.");
            return errors;
        }

        if (!Passwords.check(password, user.getPassword())) {
            errors.add("password", "The provided password does not match your current password.");
            return errors;
        }

        String avatar = user.getAvatarPath();
        List<String> covers = posts.coverPathsForUser(user.getId());

        users.delete(user.getId());

        Uploads.delete(avatar);
        covers.forEach(Uploads::delete);

        return errors;
    }
}
