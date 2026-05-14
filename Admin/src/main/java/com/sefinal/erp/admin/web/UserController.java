package com.sefinal.erp.admin.web;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.read')")
    public List<User> listForCompany(@PathVariable int companyId) {
        ensureCompany(companyId);
        return users.findByCompanyIdOrderByUserId(companyId);
    }

    @PostMapping("/api/companies/{companyId}/users")
    @Operation(summary = "Create a user in a company")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.create')")
    public ResponseEntity<User> create(@PathVariable int companyId, @RequestBody CreateUserRequest req) {
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

        CurrentUser cu = currentUser();
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "user.create", "user",
                created.getUserId(), "email=" + created.getEmail()));
        return ResponseEntity.created(URI.create("/api/users/" + created.getUserId())).body(created);
    }

    @GetMapping("/api/users/{id}")
    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.read')")
    public User get(@PathVariable int id) {
        return users.findById(id)
                .orElseThrow(() -> new NotFoundException("user " + id + " not found"));
    }

    @PutMapping("/api/users/{id}")
    @Operation(summary = "Update a user")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.update')")
    public User update(@PathVariable int id, @RequestBody com.sefinal.erp.admin.web.dto.Dtos.UpdateUserRequest req) {
        CurrentUser cu = currentUser();
        User u = users.findById(id).orElseThrow(() -> new NotFoundException("user " + id + " not found"));
        if (u.getCompanyId() != cu.companyId()) {
            throw new BadRequestException("cannot manage users from another company");
        }
        
        u.setFirstName(req.firstName());
        u.setLastName(req.lastName());
        u.setEmail(req.email());
        u.setRoleId(req.roleId());
        if (req.isActive() != null) u.setActive(req.isActive());
        if (req.mfaEnabled() != null) u.setMfaEnabled(req.mfaEnabled());
        
        User updated = users.save(u);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "user.update", "user", id, null));
        return updated;
    }

    @DeleteMapping("/api/users/{id}")
    @Operation(summary = "Delete a user")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.delete')")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        CurrentUser cu = currentUser();
        User u = users.findById(id).orElseThrow(() -> new NotFoundException("user " + id + " not found"));
        if (u.getCompanyId() != cu.companyId()) {
            throw new BadRequestException("cannot delete users from another company");
        }
        users.deleteById(id);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "user.delete", "user", id, null));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/users")
    @Operation(summary = "Get user by email")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.read')")
    public User getByEmail(@RequestParam String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user with email " + email + " not found"));
    }

    @PostMapping("/api/users/{id}/deactivate")
    @Operation(summary = "Deactivate a user")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.update')")
    public ResponseEntity<Void> deactivate(@PathVariable int id) {
        CurrentUser cu = currentUser();
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.update')")
    public ResponseEntity<Void> activate(@PathVariable int id) {
        CurrentUser cu = currentUser();
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.update')")
    public ResponseEntity<Void> resetPassword(@PathVariable int id, @RequestBody UpdatePasswordRequest req) {
        CurrentUser cu = currentUser();
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

    private static CurrentUser currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CurrentUser cu)) {
            throw new BadRequestException("no authenticated user on request");
        }
        return cu;
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
