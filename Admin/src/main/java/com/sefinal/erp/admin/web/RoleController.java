package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.model.AuditLog;
import com.sefinal.erp.admin.model.Permission;
import com.sefinal.erp.admin.model.Role;
import com.sefinal.erp.admin.repository.AuditLogRepository;
import com.sefinal.erp.admin.repository.CompanyRepository;
import com.sefinal.erp.admin.repository.RoleRepository;
import com.sefinal.erp.admin.web.dto.Dtos.CreateRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@Tag(name = "Roles", description = "Role and permission management")
public class RoleController {

    private final RoleRepository roles;
    private final CompanyRepository companies;
    private final AuditLogRepository auditRepo;

    public RoleController(RoleRepository roles, CompanyRepository companies, AuditLogRepository auditRepo) {
        this.roles     = roles;
        this.companies = companies;
        this.auditRepo = auditRepo;
    }

    @GetMapping("/api/companies/{companyId}/roles")
    @Operation(summary = "List roles for a company")
    public List<Role> listForCompany(@PathVariable int companyId) {
        ensureCompany(companyId);
        return roles.findByCompanyIdOrderByRoleId(companyId);
    }

    @PostMapping("/api/companies/{companyId}/roles")
    @Operation(summary = "Create a role in a company")
    public ResponseEntity<Role> create(@PathVariable int companyId, @RequestBody CreateRoleRequest req,
                                       HttpServletRequest request) {
        ensureCompany(companyId);
        if (req.roleName() == null || req.roleName().isBlank()) {
            throw new BadRequestException("roleName is required");
        }
        Role r = new Role();
        r.setRoleName(req.roleName());
        r.setDescription(req.description());
        r.setCompanyId(companyId);
        r.setActive(req.isActive() == null || req.isActive());
        Role created = roles.save(r);

        CurrentUser cu = AuthInterceptor.current(request);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "role.create", "role", created.getRoleId(), null));
        return ResponseEntity.created(URI.create("/api/roles/" + created.getRoleId())).body(created);
    }

    @GetMapping("/api/roles/{roleId}")
    @Operation(summary = "Get role by ID")
    public Role get(@PathVariable int roleId) {
        return roles.findById(roleId)
                .orElseThrow(() -> new NotFoundException("role " + roleId + " not found"));
    }

    @GetMapping("/api/roles/{roleId}/permissions")
    @Operation(summary = "List permissions for a role")
    public List<Permission> permissionsForRole(@PathVariable int roleId) {
        get(roleId);
        return roles.permissionsForRole(roleId);
    }

    @PostMapping("/api/roles/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Grant a permission to a role")
    public ResponseEntity<Void> grant(@PathVariable int roleId, @PathVariable int permissionId,
                                      HttpServletRequest request) {
        get(roleId);
        roles.grantPermission(roleId, permissionId);
        CurrentUser cu = AuthInterceptor.current(request);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "role.permission.grant", "role", roleId,
                "permissionId=" + permissionId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/roles/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Revoke a permission from a role")
    public ResponseEntity<Void> revoke(@PathVariable int roleId, @PathVariable int permissionId,
                                       HttpServletRequest request) {
        get(roleId);
        roles.revokePermission(roleId, permissionId);
        CurrentUser cu = AuthInterceptor.current(request);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "role.permission.revoke", "role", roleId,
                "permissionId=" + permissionId));
        return ResponseEntity.noContent().build();
    }

    private void ensureCompany(int companyId) {
        companies.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company " + companyId + " not found"));
    }
}
