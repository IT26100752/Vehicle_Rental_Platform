package com.vehiclerental.config;

import com.vehiclerental.exception.ResourceNotFoundException;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Keeps the two areas apart:
 *  - /admin/**  needs an administrator (except /admin/login)
 *  - customer pages (dashboard, bookings, payments, profile, my reviews) need a signed-in customer
 * After signing in, a customer is sent back to the page they originally asked for.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService users;

    public AuthInterceptor(UserService users) {
        this.users = users;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        HttpSession session = request.getSession(false);

        if (path.startsWith("/admin")) {
            if (path.equals("/admin/login")) {
                return true;
            }
            if (Auth.isAdmin(session)) {
                return true;
            }
            response.sendRedirect(request.getContextPath() + "/login?type=admin&required");
            return false;
        }

        // The bell badge is polled by both customers and administrators.
        if ((path.equals("/api/notifications/summary") || path.equals("/api/notifications/recent")) && Auth.isAdmin(session)) {
            return true;
        }

        // The review wall is public; writing / editing / deleting a review needs a customer.
        if ("GET".equals(request.getMethod()) && path.equals("/reviews")) {
            return true;
        }

        if (!Auth.isCustomer(session)) {
            if ("GET".equals(request.getMethod())) {
                String query = request.getQueryString();
                request.getSession(true).setAttribute(Auth.SAVED_URL, path + (query == null ? "" : "?" + query));
            }
            response.sendRedirect(request.getContextPath() + "/login?required");
            return false;
        }

        // A customer blocked or removed by the admin is signed out on the next click.
        try {
            Customer customer = users.findById(Auth.userId(session));
            if (!customer.isLoginAllowed()) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login?blocked");
                return false;
            }
        } catch (ResourceNotFoundException ex) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?required");
            return false;
        }
        return true;
    }
}
