package com.sefinal.erp.admin.model;

import jakarta.persistence.*;

@Entity
@Table(name = "modules")
public class Module {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "module_id")
    private Integer moduleId;

    @Column(name = "module_name", nullable = false, unique = true)
    private String moduleName;

    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public Module() {}

    public Integer getModuleId() { return moduleId; }
    public void setModuleId(Integer moduleId) { this.moduleId = moduleId; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
}
