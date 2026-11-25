package id.myitsbroadcast.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/** Logs uncaught request failures before the generic 500 error page masks them. */
public final class ExceptionLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable failure) {
            String route = request instanceof HttpServletRequest http
                    ? http.getMethod() + " " + http.getRequestURI()
                        + (http.getQueryString() == null ? "" : "?" + http.getQueryString())
                    : "non-HTTP request";

            // Tomcat/Railway captures ServletContext.log output with the full cause chain.
            request.getServletContext().log("[MYITS-REQUEST-ERROR] " + route, failure);

            if (failure instanceof IOException io) {
                throw io;
            }
            if (failure instanceof ServletException servlet) {
                throw servlet;
            }
            if (failure instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (failure instanceof Error error) {
                throw error;
            }
            throw new ServletException("Unhandled request failure at " + route, failure);
        }
    }
}
