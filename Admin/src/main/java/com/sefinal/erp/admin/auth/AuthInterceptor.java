package com.sefinal.erp.admin.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String CURRENT_USER_ATTR = "currentUser";
    public static final String SESSION_COOKIE = "ADMIN_SESSION";

    private final SessionStore sessions;

    public AuthInterceptor(SessionStore sessions) {
        this.sessions = sessions;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Optional<CurrentUser> user = readSessionId(request).flatMap(sessions::touch);
        if (user.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Cookie realm=\"admin\"");
            try { response.getWriter().write("{\"status\":401,\"detail\":\"authentication required\"}"); }
            catch (Exception ignored) {}
            return false;
        }
        request.setAttribute(CURRENT_USER_ATTR, user.get());
        return true;
    }

    public static CurrentUser current(HttpServletRequest request) {
        // Prefer JWT principal set by JwtAuthenticationFilter
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CurrentUser cu) {
            return cu;
        }
        // Fallback: legacy cookie-session attribute
        Object u = request.getAttribute(CURRENT_USER_ATTR);
        if (u instanceof CurrentUser cu) {
            return cu;
        }
        throw new IllegalStateException("No authenticated user on request");
    }

    private Optional<String> readSessionId(HttpServletRequest req) {
        if (req.getCookies() == null) return Optional.empty();
        for (Cookie c : req.getCookies()) {
            if (SESSION_COOKIE.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                return Optional.of(c.getValue());
            }
        }
        return Optional.empty();
    }
}
