package com.vehiclerental.config;

import com.vehiclerental.admin.entity.AdminUser;
import com.vehiclerental.common.util.SessionUtil;
import com.vehiclerental.user.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Very small "security layer" (INFORMATION HIDING of the auth mechanism).
 *  - everything needs a logged-in user except the PUBLIC paths;
 *  - ADMIN paths additionally need an AdminUser in the session.
 *
 * In the viva you can explain: "instead of checking the session in all 30
 * controller methods, one interceptor guards the whole application".
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Set<String> PUBLIC_PAGES = Set.of("/", "/login", "/register", "/logout");

    /** GET-only pages a visitor may browse without logging in. */
    private static final Pattern PUBLIC_BROWSE = Pattern.compile(
            "^/(vehicles|vehicles/\\d+|reviews|reviews/\\d+)$");

    /** Prefixes that only an AdminUser may open. */
    private static final Set<String> ADMIN_PREFIXES = Set.of(
            "/admin", "/users", "/payments/all", "/bookings/all", "/reviews/moderation");

    private static final Pattern ADMIN_VEHICLE_WRITE = Pattern.compile(
            "^/vehicles/(add|save|\\d+/(edit|update|delete))$");

    /** Only moderation decisions are admin-only; submitting/editing is for everyone. */
    private static final Pattern ADMIN_REVIEW_WRITE = Pattern.compile(
            "^/reviews/\\d+/(approve|reject)$");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) {
            path = path.substring(context.length());
        }
        // Tomcat appends ";jsessionid=.." to URLs of a brand-new session (URL
        // rewriting). Strip that path parameter so the rules below still match.
        int semi = path.indexOf(';');
        if (semi >= 0) {
            path = path.substring(0, semi);
        }

        if (PUBLIC_PAGES.contains(path) || path.startsWith("/css/") || path.startsWith("/images/")
                || path.startsWith("/error") || path.equals("/UI-BUILD.txt")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(request.getMethod()) && PUBLIC_BROWSE.matcher(path).matches()) {
            return true;
        }

        User user = request.getSession(false) == null
                ? null
                : SessionUtil.currentUser(request.getSession(false));
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        boolean adminArea = ADMIN_PREFIXES.stream().anyMatch(path::startsWith)
                || ADMIN_VEHICLE_WRITE.matcher(path).matches()
                || ADMIN_REVIEW_WRITE.matcher(path).matches();
        if (adminArea && !(user instanceof AdminUser)) {
            response.sendRedirect(request.getContextPath() + "/vehicles");
            return false;
        }
        return true;
    }
}
