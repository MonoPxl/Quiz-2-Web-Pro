package id.myitsbroadcast.filter;

import id.myitsbroadcast.util.Csrf;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * Reject state-changing requests without a valid session token. Static assets
 * are exempt because they never mutate state.
 */
public class CsrfFilter implements Filter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        if (isStatic(httpRequest) || SAFE_METHODS.contains(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        if (!Csrf.verify(httpRequest)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpRequest.setAttribute("errorTitle", "Your session expired");
            httpRequest.setAttribute("errorMessage",
                    "The form could not be verified. Go back, reload the page and try again.");
            httpRequest.getRequestDispatcher("/WEB-INF/views/errors/403.jsp").forward(httpRequest, httpResponse);
            return;
        }

        chain.doFilter(request, response);
    }

    static boolean isStatic(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());

        return path.startsWith("/assets/") || path.startsWith("/uploads/") || path.equals("/favicon.ico");
    }
}
