package id.myitsbroadcast.util;

import id.myitsbroadcast.dao.UserDao;
import id.myitsbroadcast.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Persistent "remember me" login.
 *
 * <p>The cookie carries a user id and a random validator. Only the SHA-256 hash
 * of the validator is stored on the user row, so a leaked database snapshot
 * cannot be replayed as a login cookie.</p>
 */
public final class RememberMe {

    public static final String COOKIE = "myits_remember";
    private static final int MAX_AGE = 60 * 60 * 24 * 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private RememberMe() {
    }

    public static void issue(HttpServletRequest request, HttpServletResponse response, User user) {
        byte[] random = new byte[32];
        RANDOM.nextBytes(random);
        String validator = HexFormat.of().formatHex(random);

        new UserDao().updateRememberToken(user.getId(), sha256(validator));

        Cookie cookie = new Cookie(COOKIE, user.getId() + "|" + validator);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(cookiePath(request));
        cookie.setMaxAge(MAX_AGE);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    /** Resolve a valid remembered user, or null when the cookie is absent/stale. */
    public static User attempt(HttpServletRequest request) {
        Cookie cookie = find(request);

        if (cookie == null) {
            return null;
        }

        String[] parts = cookie.getValue().split("\\|", 2);

        if (parts.length != 2) {
            return null;
        }

        long userId;

        try {
            userId = Long.parseLong(parts[0]);
        } catch (NumberFormatException exception) {
            return null;
        }

        User user = new UserDao().findById(userId);

        if (user == null || user.getRememberToken() == null
                || !MessageDigest.isEqual(
                        user.getRememberToken().getBytes(StandardCharsets.UTF_8),
                        sha256(parts[1]).getBytes(StandardCharsets.UTF_8))) {
            return null;
        }

        return user;
    }

    public static void clear(HttpServletRequest request, HttpServletResponse response, User user) {
        if (user != null) {
            new UserDao().updateRememberToken(user.getId(), null);
        }

        Cookie cookie = new Cookie(COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(cookiePath(request));
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private static Cookie find(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (COOKIE.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie;
            }
        }

        return null;
    }

    private static String cookiePath(HttpServletRequest request) {
        String context = request.getContextPath();

        return context == null || context.isEmpty() ? "/" : context;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
