package id.myitsbroadcast.service;

import id.myitsbroadcast.dao.UserDao;
import id.myitsbroadcast.model.User;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Passwords;

import java.util.regex.Pattern;

/** Registration and credential checks. Email verification is not part of this app. */
public class AuthService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserDao users = new UserDao();

    public Errors validateRegistration(String name, String email, String password, String confirmation) {
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
        } else if (users.emailExists(trimmedEmail, null)) {
            errors.add("email", "An account with this email already exists.");
        }

        for (String message : Passwords.validate(password, confirmation)) {
            errors.add("password", message);
        }

        return errors;
    }

    public User register(String name, String email, String password) {
        long id = users.create(name.trim(), email.trim(), Passwords.hash(password), false);

        return users.findById(id);
    }

    public User authenticate(String email, String password) {
        User user = users.findByEmail(email);

        if (user == null || !Passwords.check(password, user.getPassword())) {
            return null;
        }

        return user;
    }

    public User findByEmail(String email) {
        return users.findByEmail(email);
    }
}
