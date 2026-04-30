package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.dao.AuditDao;
import com.sefinal.erp.admin.dao.CompanyDao;
import com.sefinal.erp.admin.dao.PermissionDao;
import com.sefinal.erp.admin.dao.RoleDao;
import com.sefinal.erp.admin.model.Permission;
import com.sefinal.erp.admin.model.Role;
import com.sefinal.erp.admin.web.dto.Dtos.CreateRoleRequest;
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
public class RoleController {

    private final RoleDao roles;
    private final CompanyDao companies;
    private final PermissionDao permissions;
    private final AuditDao audit;

    public RoleController(RoleDao roles, CompanyDao companies, PermissionDao permissions, AuditDao audit) {
        this.roles = roles;
        this.companies = companies;
        this.permissions = permissions;
        this.audit = audit;
    }

    @GetMapping("/api/companies/{companyId}/roles")
    public List<Role> listForCompany(@PathVariable int companyId) {
        ensureCompany(companyId);
        return roles.findByCompany(companyId);
    }

    @PostMapping("/api/companies/{companyId}/roles")
    public ResponseEntity<Role> create(@PathVariable int companyId, @RequestBody CreateRoleRequest req,
                                       HttpServletRequest request) {
        ensureCompany(companyId);
        if (req.roleName() == null || req.roleName().isBlank()) {
            throw new BadRequestException("roleName is required");
        }
        Role created = roles.create(new Role(
                null,
                req.roleName(),
                req.description(),
                companyId,
                req.isActive() == null || req.isActive()
        ));
        CurrentUser cu = AuthInterceptor.current(request);
        audit.log(cu.userId(), cu.companyId(), "role.create", "role", created.roleId(), null);
        return ResponseEntity.created(URI.create("/api/roles/" + created.roleId())).body(created);
    }

    @GetMapping("/api/roles/{roleId}")
    public Role get(@PathVariable int roleId) {
        return roles.findById(roleId)
                .orElseThrow(() -> new NotFoundException("role " + roleId + " not found"));
    }

    @GetMapping("/api/roles/{roleId}/permissions")
    public List<Permission> permissionsForRole(@PathVariable int roleId) {
        get(roleId);
        return permissions.findForRole(roleId);
    }

    @PostMapping("/api/roles/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> grant(@PathVariable int roleId, @PathVariable int permissionId,
                                      HttpServletRequest request) {
        get(roleId);
        roles.grantPermission(roleId, permissionId);
        CurrentUser cu = AuthInterceptor.current(request);
        audit.log(cu.userId(), cu.companyId(), "role.permission.grant", "role", roleId,
                "permissionId=" + permissionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/roles/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> revoke(@PathVariable int roleId, @PathVariable int permissionId,
                                       HttpServletRequest request) {
        get(roleId);
        roles.revokePermission(roleId, permissionId);
        CurrentUser cu = AuthInterceptor.current(request);
        audit.log(cu.userId(), cu.companyId(), "role.permission.revoke", "role", roleId,
                "permissionId=" + permissionId);
        return ResponseEntity.noContent().build();
    }

    private void ensureCompany(int companyId) {
        companies.findById(companyId)
                .orElseThrow(() -> new NotFoundException("company " + companyId + " not found"));
    }
}
