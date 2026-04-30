package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.dao.AuditDao;
import com.sefinal.erp.admin.dao.CompanyDao;
import com.sefinal.erp.admin.dao.RoleDao;
import com.sefinal.erp.admin.dao.UserDao;
import com.sefinal.erp.admin.model.Role;
import com.sefinal.erp.admin.model.User;
import com.sefinal.erp.admin.web.dto.Dtos.CreateUserRequest;
import com.sefinal.erp.admin.web.dto.Dtos.UpdatePasswordRequest;
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
public class UserController {

    private final UserDao users;
    private final RoleDao roles;
    private final CompanyDao companies;
    private final PasswordEncoder passwordEncoder;
    private final AuditDao audit;

    public UserController(UserDao users, RoleDao roles, CompanyDao companies,
                          PasswordEncoder passwordEncoder, AuditDao audit) {
        this.users = users;
        this.roles = roles;
        this.companies = companies;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
    }

    @GetMapping("/api/companies/{companyId}/users")
    public List<User> listForCompany(@PathVariable int companyId) {
        ensureCompany(companyId);
        return users.findByCompany(companyId);
    }

    @PostMapping("/api/companies/{companyId}/users")
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
            if (!role.companyId().equals(companyId)) {
                throw new BadRequestException("role " + req.roleId() + " belongs to a different company");
            }
        }

        User created = users.create(new User(
                null,
                req.firstName(),
                req.lastName(),
                req.email(),
                passwordEncoder.encode(req.password()),
                companyId,
                req.roleId(),
                req.isActive()  == null || req.isActive(),
                req.mfaEnabled() != null && req.mfaEnabled(),
                0,
                null,
                null
        ));
        CurrentUser cu = AuthInterceptor.current(request);
        audit.log(cu.userId(), cu.companyId(), "user.create", "user", created.userId(), "email=" + created.email());
        return ResponseEntity.created(URI.create("/api/users/" + created.userId())).body(created);
    }

    @GetMapping("/api/users/{id}")
    public User get(@PathVariable int id) {
        return users.findById(id)
                .orElseThrow(() -> new NotFoundException("user " + id + " not found"));
    }

    @GetMapping("/api/users")
    public User getByEmail(@RequestParam String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user with email " + email + " not found"));
    }

    @PostMapping("/api/users/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable int id, HttpServletRequest request) {
        CurrentUser cu = AuthInterceptor.current(request);
        User u = users.findById(id).orElseThrow(() -> new NotFoundException("user " + id + " not found"));
        if (u.companyId() != cu.companyId()) {
            throw new BadRequestException("cannot manage users from another company");
        }
        users.setActive(id, false);
        audit.log(cu.userId(), cu.companyId(), "user.deactivate", "user", id, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/users/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable int id, HttpServletRequest request) {
        CurrentUser cu = AuthInterceptor.current(request);
        User u = users.findById(id).orElseThrow(() -> new NotFoundException("user " + id + " not found"));
        if (u.companyId() != cu.companyId()) {
            throw new BadRequestException("cannot manage users from another company");
        }
        users.setActive(id, true);
        audit.log(cu.userId(), cu.companyId(), "user.activate", "user", id, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/users/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable int id, @RequestBody UpdatePasswordRequest req,
                                              HttpServletRequest request) {
        CurrentUser cu = AuthInterceptor.current(request);
        User u = users.findById(id).orElseThrow(() -> new NotFoundException("user " + id + " not found"));
        if (u.companyId() != cu.companyId()) {
            throw new BadRequestException("cannot manage users from another company");
        }
        if (req.newPassword() == null || req.newPassword().isBlank()) {
            throw new BadRequestException("newPassword is required");
        }
        users.updatePassword(id, passwordEncoder.encode(req.newPassword()));
        audit.log(cu.userId(), cu.companyId(), "user.password.reset", "user", id, null);
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
