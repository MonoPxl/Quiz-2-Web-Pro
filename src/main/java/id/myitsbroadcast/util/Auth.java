package id.myitsbroadcast.util;

import id.myitsbroadcast.dao.UserDao;
import id.myitsbroadcast.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Session authentication helpers with fixation protection. */
public final class Auth {

    public static final String SESSION_KEY = "auth_user_id";
    private static final String REQUEST_KEY = "current_user";

    private static final UserDao USERS = new UserDao();

    private Auth() {
    }

    public static User user(HttpServletRequest request) {
        Object cached = request.getAttribute(REQUEST_KEY);

        if (cached instanceof User user) {
            return user;
        }

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object stored = session.getAttribute(SESSION_KEY);

        if (!(stored instanceof Long id)) {
            return null;
        }

        User user = USERS.findById(id);

        if (user == null) {
            session.removeAttribute(SESSION_KEY);
            return null;
        }

        request.setAttribute(REQUEST_KEY, user);

        return user;
    }

    public static boolean check(HttpServletRequest request) {
        return user(request) != null;
    }

    /** Log in with a brand-new session id, defeating session fixation. */
    public static void login(HttpServletRequest request, HttpServletResponse response,
                             User user, boolean remember) {
        HttpSession existing = request.getSession(false);

        if (existing != null) {
            existing.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_KEY, user.getId());
        session.setMaxInactiveInterval(60 * 60 * 2);
        request.setAttribute(REQUEST_KEY, user);

        if (remember) {
            RememberMe.issue(request, response, user);
        }
    }

    public static void logout(HttpServletRequest request, HttpServletResponse response) {
        User user = user(request);

        RememberMe.clear(request, response, user);

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        request.removeAttribute(REQUEST_KEY);
    }
}
