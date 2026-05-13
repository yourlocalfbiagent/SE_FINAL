package com.sefinal.erp.admin.repository;

import com.sefinal.erp.admin.model.Permission;
import com.sefinal.erp.admin.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    List<Role> findByCompanyIdOrderByRoleId(int companyId);

    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.roleId = :id")
    Optional<Role> findWithPermissions(@Param("id") int id);

    @Modifying @Transactional
    @Query(value = "INSERT INTO role_permissions (role_id, permission_id) VALUES (:rid, :pid) ON CONFLICT DO NOTHING",
           nativeQuery = true)
    void grantPermission(@Param("rid") int roleId, @Param("pid") int permissionId);

    @Modifying @Transactional
    @Query(value = "DELETE FROM role_permissions WHERE role_id = :rid AND permission_id = :pid",
           nativeQuery = true)
    void revokePermission(@Param("rid") int roleId, @Param("pid") int permissionId);

    @Query(value = "SELECT COUNT(*) > 0 FROM role_permissions rp " +
                   "JOIN permissions p ON p.permission_id = rp.permission_id " +
                   "JOIN modules m ON m.module_id = p.module_id " +
                   "JOIN actions a ON a.action_id = p.action_id " +
                   "WHERE rp.role_id = ?1 AND m.module_name = ?2 AND a.action_name = ?3",
           nativeQuery = true)
    boolean hasPermission(int roleId, String moduleName, String actionName);

    long countByCompanyId(int companyId);

    long countByCompanyIdAndIsActive(int companyId, boolean isActive);

    default List<Permission> permissionsForRole(int roleId) {
        return findWithPermissions(roleId)
                .map(r -> List.copyOf(r.getPermissions()))
                .orElse(List.of());
    }
}
