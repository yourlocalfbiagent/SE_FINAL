package com.sefinal.erp.controller;

import com.sefinal.erp.entity.User;
import com.app.se_final_sales.repository.UserRepository;
import com.sefinal.erp.repository.RoleRepository;
import com.sefinal.erp.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Login, logout, and current user")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.lockout.duration-minutes:15}")
    private long lockMinutes;

    @PostMapping("/login")
    @Operation(summary = "Login — returns JWT Bearer token")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("detail", "email and password are required"));
        }

        var maybeUser = userRepo.findByEmail(email);
        if (maybeUser.isEmpty() || !Boolean.TRUE.equals(maybeUser.get().getIsActive())) {
            return unauthorized("invalid credentials");
        }
        User u = maybeUser.get();

        if (u.getLockedUntil() != null && u.getLockedUntil().isAfter(LocalDateTime.now())) {
            return unauthorized("account is locked until " + u.getLockedUntil());
        }

        if (!passwordEncoder.matches(password, u.getPasswordHash())) {
            userRepo.incrementFailedAttempts(u.getUserId());
            int attempts = userRepo.findById(u.getUserId())
                    .map(User::getFailedLoginAttempts).orElse(maxAttempts);
            if (attempts >= maxAttempts) {
                userRepo.lockUntil(u.getUserId(), LocalDateTime.now().plusMinutes(lockMinutes));
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

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", u.getUserId(),
                "email", u.getEmail(),
                "companyId", u.getCompanyId() != null ? u.getCompanyId() : 0,
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
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Claims claims)) {
            return unauthorized("not authenticated");
        }
        return ResponseEntity.ok(Map.of(
                "userId", claims.get("userId"),
                "email", claims.getSubject(),
                "companyId", claims.get("companyId"),
                "role", claims.get("role") != null ? claims.get("role") : ""
        ));
    }

    private static ResponseEntity<?> unauthorized(String detail) {
        return ResponseEntity.status(401).body(Map.of("status", 401, "detail", detail));
    }
}
