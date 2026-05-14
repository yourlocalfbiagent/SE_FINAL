package com.sefinal.erp.admin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "modules")
@Getter @Setter @NoArgsConstructor
public class Module {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "module_id")
    private Integer moduleId;

    @Column(name = "module_name", nullable = false, unique = true)
    private String moduleName;

    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
