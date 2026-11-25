package id.myitsbroadcast.filter;

import id.myitsbroadcast.model.User;
import id.myitsbroadcast.util.Auth;
import id.myitsbroadcast.util.RememberMe;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication gate for protected routes, guest-only auth screens, and
 * transparent "remember me" sign-in.
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        if (CsrfFilter.isStatic(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        String method = httpRequest.getMethod();

        if (Auth.user(httpRequest) == null && !isAuthScreen(path)) {
            User remembered = RememberMe.attempt(httpRequest);

            if (remembered != null && ("GET".equals(method) || "HEAD".equals(method))) {
                Auth.login(httpRequest, httpResponse, remembered, true);
            }
        }

        boolean authenticated = Auth.check(httpRequest);

        if (isGuestOnly(path) && authenticated) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/dashboard");
            return;
        }

        if (requiresAuthentication(path, method) && !authenticated) {
            if (wantsJson(httpRequest)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"error\":\"unauthenticated\"}");
                return;
            }

            HttpSession session = httpRequest.getSession(true);
            String query = httpRequest.getQueryString();
            session.setAttribute("intended_url", path + (query == null ? "" : "?" + query));

            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        chain.doFilter(request, response);
    }

    private static boolean requiresAuthentication(String path, String method) {
        return path.equals("/dashboard")
                || path.startsWith("/settings")
                || path.equals("/comments")
                || path.startsWith("/comments/")
                || path.equals("/bookmarks")
                || path.equals("/confirm-password")
                || path.equals("/posts/create")
                || (path.startsWith("/posts/") && ("POST".equals(method) || path.endsWith("/edit")));
    }

    private static boolean isAuthScreen(String path) {
        return isGuestOnly(path) || path.equals("/confirm-password");
    }

    private static boolean isGuestOnly(String path) {
        return path.equals("/login")
                || path.equals("/register");
    }

    private static boolean wantsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");

        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
                || (accept != null && accept.contains("application/json"));
    }
}
