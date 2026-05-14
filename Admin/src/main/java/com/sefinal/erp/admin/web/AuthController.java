package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.model.AuditLog;
import com.sefinal.erp.admin.model.Company;
import com.sefinal.erp.admin.model.Role;
import com.sefinal.erp.admin.model.User;
import com.sefinal.erp.admin.repository.AuditLogRepository;
import com.sefinal.erp.admin.repository.CompanyRepository;
import com.sefinal.erp.admin.repository.PermissionRepository;
import com.sefinal.erp.admin.repository.RoleRepository;
import com.sefinal.erp.admin.repository.UserRepository;
import com.sefinal.erp.admin.security.JwtTokenProvider;
import com.sefinal.erp.admin.web.dto.Dtos.LoginRequest;
import com.sefinal.erp.admin.web.dto.Dtos.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Login, logout, and session validation")
public class AuthController {

    private final UserRepository userRepo;
    private final AuditLogRepository auditRepo;
    private final RoleRepository roleRepo;
    private final CompanyRepository companyRepo;
    private final PermissionRepository permissionRepo;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final int maxAttempts;
    private final long lockMinutes;

    public AuthController(UserRepository userRepo, AuditLogRepository auditRepo, RoleRepository roleRepo,
                          CompanyRepository companyRepo, PermissionRepository permissionRepo,
                          JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder,
                          @Value("${app.lockout.max-attempts:5}") int maxAttempts,
                          @Value("${app.lockout.duration-minutes:15}") long lockMinutes) {
        this.userRepo         = userRepo;
        this.auditRepo        = auditRepo;
        this.roleRepo         = roleRepo;
        this.companyRepo      = companyRepo;
        this.permissionRepo   = permissionRepo;
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
        List<String> perms = Collections.emptyList();
        if (u.getRoleId() != null) {
            var roleOpt = roleRepo.findWithPermissions(u.getRoleId());
            if (roleOpt.isPresent()) {
                Role r = roleOpt.get();
                roleName = r.getRoleName();
                if ("ADMIN".equalsIgnoreCase(roleName)) {
                    // Admins get ALL permissions automatically
                    perms = permissionRepo.findAll().stream()
                            .map(p -> p.getModuleName() + "." + p.getActionName())
                            .collect(Collectors.toList());
                } else {
                    perms = r.getPermissions().stream()
                            .map(p -> p.getModuleName() + "." + p.getActionName())
                            .collect(Collectors.toList());
                }
            }
        }

        String token = jwtTokenProvider.generateToken(
                u.getUserId(), u.getCompanyId(), u.getEmail(), u.getRoleId(), roleName, perms);

        auditRepo.save(new AuditLog(u.getUserId(), u.getCompanyId(), "user.login", "user", u.getUserId(), null));

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", u.getUserId(),
                "companyId", u.getCompanyId(),
                "email", u.getEmail(),
                "role", roleName
        ));
    }

    @PostMapping("/register")
    @Transactional
    @Operation(summary = "Register a new company with an admin user — returns JWT Bearer token")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.companyName() == null || req.companyName().isBlank())
            throw new BadRequestException("companyName is required");
        if (req.firstName() == null || req.firstName().isBlank())
            throw new BadRequestException("firstName is required");
        if (req.lastName() == null || req.lastName().isBlank())
            throw new BadRequestException("lastName is required");
        if (req.email() == null || req.email().isBlank())
            throw new BadRequestException("email is required");
        if (req.password() == null || req.password().length() < 8)
            throw new BadRequestException("password must be at least 8 characters");

        if (userRepo.findByEmail(req.email()).isPresent())
            throw new ConflictException("an account with this email already exists");

        // 1. Create company
        Company company = new Company();
        company.setCompanyName(req.companyName().trim());
        company.setCurrency("USD");
        company.setTaxDefault(BigDecimal.ZERO);
        company.setLocale("en-US");
        company.setActive(true);
        company = companyRepo.save(company);

        // 2. Create ADMIN role for this company
        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN");
        adminRole.setDescription("System administrator with full access");
        adminRole.setCompanyId(company.getCompanyId());
        adminRole.setActive(true);
        // Explicitly grant all existing permissions to the initial ADMIN role
        adminRole.setPermissions(new java.util.HashSet<>(permissionRepo.findAll()));
        adminRole = roleRepo.save(adminRole);

        // 3. Create the admin user
        User user = new User();
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        user.setEmail(req.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setCompanyId(company.getCompanyId());
        user.setRoleId(adminRole.getRoleId());
        user.setActive(true);
        user.setMfaEnabled(false);
        user.setFailedLoginAttempts(0);
        user = userRepo.save(user);

        auditRepo.save(new AuditLog(user.getUserId(), company.getCompanyId(),
                "company.register", "company", company.getCompanyId(),
                "admin=" + user.getEmail()));

        // Fetch all permissions for the initial ADMIN JWT
        List<String> allPerms = permissionRepo.findAll().stream()
                .map(p -> p.getModuleName() + "." + p.getActionName())
                .collect(Collectors.toList());

        String token = jwtTokenProvider.generateToken(
                user.getUserId(), company.getCompanyId(), user.getEmail(),
                adminRole.getRoleId(), "ADMIN", allPerms);

        return ResponseEntity.status(201).body(Map.of(
                "token",       token,
                "userId",      user.getUserId(),
                "companyId",   company.getCompanyId(),
                "email",       user.getEmail(),
                "role",        "ADMIN",
                "companyName", company.getCompanyName()
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
