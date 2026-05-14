package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.model.AuditLog;
import com.sefinal.erp.admin.model.Permission;
import com.sefinal.erp.admin.model.Role;
import com.sefinal.erp.admin.repository.AuditLogRepository;
import com.sefinal.erp.admin.repository.CompanyRepository;
import com.sefinal.erp.admin.repository.PermissionRepository;
import com.sefinal.erp.admin.repository.RoleRepository;
import com.sefinal.erp.admin.web.dto.Dtos.CreateRoleRequest;
import com.sefinal.erp.admin.web.dto.Dtos.UpdateRolePermissionsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Roles", description = "Role and permission management")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.read')")
public class RoleController {

    private final RoleRepository roles;
    private final CompanyRepository companies;
    private final PermissionRepository permissions;
    private final AuditLogRepository auditRepo;

    public RoleController(RoleRepository roles, CompanyRepository companies, PermissionRepository permissions, AuditLogRepository auditRepo) {
        this.roles     = roles;
        this.companies = companies;
        this.permissions = permissions;
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.create')")
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

    @PutMapping("/api/roles/{roleId}")
    @Operation(summary = "Update a role")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.update')")
    public Role update(@PathVariable int roleId, @RequestBody com.sefinal.erp.admin.web.dto.Dtos.UpdateRoleRequest req,
                       HttpServletRequest request) {
        Role r = get(roleId);
        r.setRoleName(req.roleName());
        r.setDescription(req.description());
        if (req.isActive() != null) r.setActive(req.isActive());
        Role updated = roles.save(r);

        CurrentUser cu = AuthInterceptor.current(request);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "role.update", "role", roleId, null));
        return updated;
    }

    @DeleteMapping("/api/roles/{roleId}")
    @Operation(summary = "Delete a role")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.delete')")
    public ResponseEntity<Void> delete(@PathVariable int roleId, HttpServletRequest request) {
        Role r = get(roleId);
        roles.deleteById(roleId);
        CurrentUser cu = AuthInterceptor.current(request);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "role.delete", "role", roleId, null));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/roles/{roleId}/permissions")
    @Operation(summary = "List permissions for a role")
    public List<Permission> permissionsForRole(@PathVariable int roleId) {
        get(roleId);
        return roles.permissionsForRole(roleId);
    }

    @PostMapping("/api/roles/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Grant a permission to a role")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.update')")
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.update')")
    public ResponseEntity<Void> revoke(@PathVariable int roleId, @PathVariable int permissionId,
                                       HttpServletRequest request) {
        get(roleId);
        roles.revokePermission(roleId, permissionId);
        CurrentUser cu = AuthInterceptor.current(request);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "role.permission.revoke", "role", roleId,
                "permissionId=" + permissionId));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/roles/{roleId}/permissions")
    @Operation(summary = "Replace all permissions for a role")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.update')")
    public ResponseEntity<Void> updatePermissions(@PathVariable int roleId, @RequestBody UpdateRolePermissionsRequest req,
                                                  HttpServletRequest request) {
        Role r = roles.findWithPermissions(roleId)
                .orElseThrow(() -> new NotFoundException("role " + roleId + " not found"));

        List<Permission> newPerms = permissions.findAllById(req.permissionIds());
        r.setPermissions(new HashSet<>(newPerms));
        roles.save(r);

        CurrentUser cu = AuthInterceptor.current(request);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "role.permissions.update", "role", roleId,
                "count=" + newPerms.size()));
        return ResponseEntity.noContent().build();
    }

    private void ensureCompany(int companyId) {
        companies.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company " + companyId + " not found"));
    }
}
