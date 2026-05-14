package com.sefinal.erp.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Integer permissionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    @JsonIgnore
    private Module module;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "action_id", nullable = false)
    @JsonIgnore
    private Action action;

    private String description;

    public Permission() {}

    public Integer getPermissionId() { return permissionId; }
    public void setPermissionId(Integer permissionId) { this.permissionId = permissionId; }

    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }

    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getModuleId()   { return module != null ? module.getModuleId()   : null; }
    public String  getModuleName() { return module != null ? module.getModuleName() : null; }
    public Integer getActionId()   { return action != null ? action.getActionId()   : null; }
    public String  getActionName() { return action != null ? action.getActionName() : null; }
}
