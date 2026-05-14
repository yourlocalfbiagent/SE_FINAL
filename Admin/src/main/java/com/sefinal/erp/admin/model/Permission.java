package com.sefinal.erp.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permissions")
@Getter @Setter @NoArgsConstructor
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

    public Integer getModuleId()   { return module != null ? module.getModuleId()   : null; }
    public String  getModuleName() { return module != null ? module.getModuleName() : null; }
    public Integer getActionId()   { return action != null ? action.getActionId()   : null; }
    public String  getActionName() { return action != null ? action.getActionName() : null; }
}
