package com.sefinal.erp.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    private String description;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public Role() {}

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCompanyId() { return companyId; }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
}
