package id.myitsbroadcast.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** One-shot session messages consumed by the shared layout. */
public final class Flash {

    private static final String STATUS = "flash_status";
    private static final String ERROR = "flash_error";

    private Flash() {
    }

    public static void success(HttpSession session, String message) {
        session.setAttribute(STATUS, message);
    }

    public static void error(HttpSession session, String message) {
        session.setAttribute(ERROR, message);
    }

    /**
     * Move pending messages from the session to the request so the redirect
     * target renders them exactly once.
     */
    public static void consume(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        Object status = session.getAttribute(STATUS);
        Object error = session.getAttribute(ERROR);

        if (status != null) {
            request.setAttribute("statusMessage", status);
            session.removeAttribute(STATUS);
        }

        if (error != null) {
            request.setAttribute("errorMessage", error);
            session.removeAttribute(ERROR);
        }
    }
}
