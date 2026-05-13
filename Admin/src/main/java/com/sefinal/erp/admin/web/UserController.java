package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.model.AuditLog;
import com.sefinal.erp.admin.model.Role;
import com.sefinal.erp.admin.model.User;
import com.sefinal.erp.admin.repository.AuditLogRepository;
import com.sefinal.erp.admin.repository.CompanyRepository;
import com.sefinal.erp.admin.repository.RoleRepository;
import com.sefinal.erp.admin.repository.UserRepository;
import com.sefinal.erp.admin.web.dto.Dtos.CreateUserRequest;
import com.sefinal.erp.admin.web.dto.Dtos.UpdatePasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@Tag(name = "Users", description = "User management within a company")
public class UserController {

    private final UserRepository users;
    private final RoleRepository roles;
    private final CompanyRepository companies;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditRepo;

    public UserController(UserRepository users, RoleRepository roles, CompanyRepository companies,
                          PasswordEncoder passwordEncoder, AuditLogRepository auditRepo) {
        this.users           = users;
        this.roles           = roles;
        this.companies       = companies;
        this.passwordEncoder = passwordEncoder;
        this.auditRepo       = auditRepo;
    }

    @GetMapping("/api/companies/{companyId}/users")
    @Operation(summary = "List users for a company")
    public List<User> listForCompany(@PathVariable int companyId) {
        ensureCompany(companyId);
        return users.findByCompanyIdOrderByUserId(companyId);
    }

    @PostMapping("/api/companies/{companyId}/users")
    @Operation(summary = "Create a user in a company")
    public ResponseEntity<User> create(@PathVariable int companyId, @RequestBody CreateUserRequest req,
                                       HttpServletRequest request) {
        ensureCompany(companyId);
        require("firstName", req.firstName());
        require("lastName",  req.lastName());
        require("email",     req.email());
        require("password",  req.password());

        if (req.roleId() != null) {
            Role role = roles.findById(req.roleId())
                    .orElseThrow(() -> new NotFoundException("role " + req.roleId() + " not found"));
            if (!role.getCompanyId().equals(companyId)) {
                throw new BadRequestException("role " + req.roleId() + " belongs to a different company");
            }
        }

        User u = new User();
        u.setFirstName(req.firstName());
        u.setLastName(req.lastName());
        u.setEmail(req.email());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setCompanyId(companyId);
        u.setRoleId(req.roleId());
        u.setActive(req.isActive() == null || req.isActive());
        u.setMfaEnabled(req.mfaEnabled() != null && req.mfaEnabled());
        u.setFailedLoginAttempts(0);
        User created = users.save(u);

        CurrentUser cu = AuthInterceptor.current(request);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "user.create", "user",
                created.getUserId(), "email=" + created.getEmail()));
        return ResponseEntity.created(URI.create("/api/users/" + created.getUserId())).body(created);
    }

    @GetMapping("/api/users/{id}")
    @Operation(summary = "Get user by ID")
    public User get(@PathVariable int id) {
        return users.findById(id)
                .orElseThrow(() -> new NotFoundException("user " + id + " not found"));
    }

    @GetMapping("/api/users")
    @Operation(summary = "Get user by email")
    public User getByEmail(@RequestParam String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user with email " + email + " not found"));
    }

    @PostMapping("/api/users/{id}/deactivate")
    @Operation(summary = "Deactivate a user")
    public ResponseEntity<Void> deactivate(@PathVariable int id, HttpServletRequest request) {
        CurrentUser cu = AuthInterceptor.current(request);
        User u = users.findById(id).orElseThrow(() -> new NotFoundException("user " + id + " not found"));
        if (u.getCompanyId() != cu.companyId()) {
            throw new BadRequestException("cannot manage users from another company");
        }
        users.setActive(id, false);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "user.deactivate", "user", id, null));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/users/{id}/activate")
    @Operation(summary = "Activate a user")
    public ResponseEntity<Void> activate(@PathVariable int id, HttpServletRequest request) {
        CurrentUser cu = AuthInterceptor.current(request);
        User u = users.findById(id).orElseThrow(() -> new NotFoundException("user " + id + " not found"));
        if (u.getCompanyId() != cu.companyId()) {
            throw new BadRequestException("cannot manage users from another company");
        }
        users.setActive(id, true);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "user.activate", "user", id, null));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/users/{id}/reset-password")
    @Operation(summary = "Reset a user's password")
    public ResponseEntity<Void> resetPassword(@PathVariable int id, @RequestBody UpdatePasswordRequest req,
                                              HttpServletRequest request) {
        CurrentUser cu = AuthInterceptor.current(request);
        User u = users.findById(id).orElseThrow(() -> new NotFoundException("user " + id + " not found"));
        if (u.getCompanyId() != cu.companyId()) {
            throw new BadRequestException("cannot manage users from another company");
        }
        if (req.newPassword() == null || req.newPassword().isBlank()) {
            throw new BadRequestException("newPassword is required");
        }
        users.updatePassword(id, passwordEncoder.encode(req.newPassword()));
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "user.password.reset", "user", id, null));
        return ResponseEntity.noContent().build();
    }

    private void ensureCompany(int companyId) {
        companies.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company " + companyId + " not found"));
    }

    private static void require(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(field + " is required");
        }
    }
}
