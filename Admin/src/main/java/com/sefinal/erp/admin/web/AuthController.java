package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.model.AuditLog;
import com.sefinal.erp.admin.model.User;
import com.sefinal.erp.admin.repository.AuditLogRepository;
import com.sefinal.erp.admin.repository.RoleRepository;
import com.sefinal.erp.admin.repository.UserRepository;
import com.sefinal.erp.admin.security.JwtTokenProvider;
import com.sefinal.erp.admin.web.dto.Dtos.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Login, logout, and session validation")
public class AuthController {

    private final UserRepository userRepo;
    private final AuditLogRepository auditRepo;
    private final RoleRepository roleRepo;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final int maxAttempts;
    private final long lockMinutes;

    public AuthController(UserRepository userRepo, AuditLogRepository auditRepo, RoleRepository roleRepo,
                          JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder,
                          @Value("${app.lockout.max-attempts:5}") int maxAttempts,
                          @Value("${app.lockout.duration-minutes:15}") long lockMinutes) {
        this.userRepo         = userRepo;
        this.auditRepo        = auditRepo;
        this.roleRepo         = roleRepo;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder  = passwordEncoder;
        this.maxAttempts      = maxAttempts;
        this.lockMinutes      = lockMinutes;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password — returns JWT Bearer token")
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

        String roleName = "EMPLOYEE";
        if (u.getRoleId() != null) {
            roleName = roleRepo.findById(u.getRoleId())
                    .map(r -> r.getRoleName())
                    .orElse("EMPLOYEE");
        }

        String token = jwtTokenProvider.generateToken(
                u.getUserId(), u.getCompanyId(), u.getEmail(), u.getRoleId(), roleName);

        auditRepo.save(new AuditLog(u.getUserId(), u.getCompanyId(), "user.login", "user", u.getUserId(), null));

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", u.getUserId(),
                "companyId", u.getCompanyId(),
                "email", u.getEmail(),
                "role", roleName
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — client should discard the JWT")
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user from JWT")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CurrentUser cu)) {
            return unauthorized("not authenticated");
        }
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
}
