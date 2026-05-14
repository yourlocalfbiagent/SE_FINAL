package com.sefinal.erp.admin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "actions")
@Getter @Setter @NoArgsConstructor
public class Action {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Integer actionId;

    @Column(name = "action_name", nullable = false, unique = true)
    private String actionName;

    private String description;
}
