package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.auth.SessionStore;
import com.sefinal.erp.admin.model.AuditLog;
import com.sefinal.erp.admin.model.User;
import com.sefinal.erp.admin.repository.AuditLogRepository;
import com.sefinal.erp.admin.repository.UserRepository;
import com.sefinal.erp.admin.web.dto.Dtos.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final AuditLogRepository auditRepo;
    private final SessionStore sessions;
    private final PasswordEncoder passwordEncoder;
    private final int maxAttempts;
    private final long lockMinutes;

    public AuthController(UserRepository userRepo, AuditLogRepository auditRepo, SessionStore sessions,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.lockout.max-attempts:5}") int maxAttempts,
                          @Value("${app.lockout.duration-minutes:15}") long lockMinutes) {
        this.userRepo        = userRepo;
        this.auditRepo       = auditRepo;
        this.sessions        = sessions;
        this.passwordEncoder = passwordEncoder;
        this.maxAttempts     = maxAttempts;
        this.lockMinutes     = lockMinutes;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req == null || req.email() == null || req.password() == null) {
            throw new BadRequestException("email and password are required");
        }

        var maybeUser = userRepo.findByEmail(req.email());
        if (maybeUser.isEmpty() || !maybeUser.get().isActive()) {
            return unauthorized("invalid credentials");
        }
        User u = maybeUser.get();

        if (u.getLockedUntil() != null && u.getLockedUntil().isAfter(LocalDateTime.now())) {
            return unauthorized("account is locked until " + u.getLockedUntil());
        }

        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            userRepo.incrementFailedAttempts(u.getUserId());
            int attempts = userRepo.findById(u.getUserId())
                    .map(User::getFailedLoginAttempts).orElse(maxAttempts);
            if (attempts >= maxAttempts) {
                LocalDateTime until = LocalDateTime.now().plusMinutes(lockMinutes);
                userRepo.lockUntil(u.getUserId(), until);
                auditRepo.save(new AuditLog(u.getUserId(), u.getCompanyId(), "user.locked",
                        "user", u.getUserId(),
                        "auto-locked after " + attempts + " failed attempts; until=" + until));
            }
            return unauthorized("invalid credentials");
        }

        userRepo.clearLoginCounters(u.getUserId());
        String sessionId = sessions.create(new CurrentUser(
                u.getUserId(), u.getCompanyId(), u.getEmail(), u.getRoleId()));
        auditRepo.save(new AuditLog(u.getUserId(), u.getCompanyId(), "user.login", "user", u.getUserId(), null));

        ResponseCookie cookie = ResponseCookie.from(AuthInterceptor.SESSION_COOKIE, sessionId)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .build();
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(meBody(u));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String cookie = readCookie(request);
        if (cookie != null) sessions.invalidate(cookie);
        ResponseCookie cleared = ResponseCookie.from(AuthInterceptor.SESSION_COOKIE, "")
                .httpOnly(true).sameSite("Lax").path("/").maxAge(0).build();
        return ResponseEntity.noContent().header("Set-Cookie", cleared.toString()).build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        String cookie = readCookie(request);
        var current = cookie == null ? java.util.Optional.<CurrentUser>empty() : sessions.touch(cookie);
        if (current.isEmpty()) return unauthorized("not authenticated");
        CurrentUser cu = current.get();
        return ResponseEntity.ok(Map.of(
                "userId", cu.userId(),
                "companyId", cu.companyId(),
                "email", cu.email(),
                "roleId", cu.roleId() == null ? "" : cu.roleId()
        ));
    }

    private static ResponseEntity<?> unauthorized(String detail) {
        return ResponseEntity.status(401).body(Map.of("status", 401, "detail", detail));
    }

    private static String readCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        for (var c : req.getCookies()) {
            if (AuthInterceptor.SESSION_COOKIE.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private static Map<String, Object> meBody(User u) {
        return Map.of(
                "userId", u.getUserId(),
                "companyId", u.getCompanyId(),
                "email", u.getEmail(),
                "roleId", u.getRoleId() == null ? "" : u.getRoleId()
        );
    }
}
