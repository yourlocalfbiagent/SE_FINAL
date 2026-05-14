package com.sefinal.erp.admin.model;

import jakarta.persistence.*;

@Entity
@Table(name = "actions")
public class Action {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Integer actionId;

    @Column(name = "action_name", nullable = false, unique = true)
    private String actionName;

    private String description;

    public Action() {}

    public Integer getActionId() { return actionId; }
    public void setActionId(Integer actionId) { this.actionId = actionId; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
