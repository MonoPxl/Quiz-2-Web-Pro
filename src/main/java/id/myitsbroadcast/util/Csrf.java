package id.myitsbroadcast.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Session-bound synchroniser tokens for every state-changing request. */
public final class Csrf {

    public static final String SESSION_KEY = "csrf_token";
    public static final String PARAM = "_csrf";
    public static final String HEADER = "X-CSRF-Token";

    private static final SecureRandom RANDOM = new SecureRandom();

    private Csrf() {
    }

    public static String token(HttpSession session) {
        Object existing = session.getAttribute(SESSION_KEY);

        if (existing instanceof String token && !token.isBlank()) {
            return token;
        }

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        session.setAttribute(SESSION_KEY, token);

        return token;
    }

    public static String token(HttpServletRequest request) {
        return token(request.getSession(true));
    }

    public static boolean verify(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object expected = session == null ? null : session.getAttribute(SESSION_KEY);

        if (!(expected instanceof String token) || token.isBlank()) {
            return false;
        }

        String submitted = request.getHeader(HEADER);

        if (submitted == null || submitted.isBlank()) {
            submitted = request.getParameter(PARAM);
        }

        return matches(token, submitted);
    }

    /** Constant-time comparison, exposed for focused unit tests. */
    static boolean matches(String expected, String submitted) {
        if (expected == null || submitted == null) {
            return false;
        }

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8));
    }
}
