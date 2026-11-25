package id.myitsbroadcast.util;

import id.myitsbroadcast.config.AppConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

/** BCrypt hashing plus the source application's password policy. */
public final class Passwords {

    private static final int WORK_FACTOR = 12;

    private Passwords() {
    }

    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verify a password against a hash.
     *
     * <p>Laravel writes BCrypt hashes with the {@code $2y$} revision marker while
     * jBCrypt only accepts {@code $2a$}. The algorithms are identical, so the
     * marker is normalised for verification and the stored hash is left
     * untouched (important for rows migrated from the legacy database).</p>
     */
    public static boolean check(String rawPassword, String hash) {
        if (rawPassword == null || hash == null || hash.isBlank()) {
            return false;
        }

        String normalised = hash.startsWith("$2y$") ? "$2a$" + hash.substring(4) : hash;

        try {
            return BCrypt.checkpw(rawPassword, normalised);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * Validate a new password. Development keeps Laravel's default minimum of
     * eight characters; production applies the stricter twelve-character,
     * mixed-case, number and symbol policy configured by the source app.
     */
    public static List<String> validate(String password, String confirmation) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("The password field is required.");
            return errors;
        }

        if (!password.equals(confirmation)) {
            errors.add("The password confirmation does not match.");
        }

        if (AppConfig.production()) {
            if (password.length() < 12) {
                errors.add("The password must be at least 12 characters.");
            }
            if (!password.matches(".*[a-z].*") || !password.matches(".*[A-Z].*")) {
                errors.add("The password must contain both uppercase and lowercase letters.");
            }
            if (!password.matches(".*[0-9].*")) {
                errors.add("The password must contain at least one number.");
            }
            if (!password.matches(".*[^A-Za-z0-9].*")) {
                errors.add("The password must contain at least one symbol.");
            }
        } else if (password.length() < 8) {
            errors.add("The password must be at least 8 characters.");
        }

        return errors;
    }
}
