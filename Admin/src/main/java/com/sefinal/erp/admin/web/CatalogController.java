package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.model.Action;
import com.sefinal.erp.admin.model.Module;
import com.sefinal.erp.admin.model.Permission;
import com.sefinal.erp.admin.repository.ActionRepository;
import com.sefinal.erp.admin.repository.ModuleRepository;
import com.sefinal.erp.admin.repository.PermissionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final ModuleRepository modules;
    private final ActionRepository actions;
    private final PermissionRepository permissions;

    public CatalogController(ModuleRepository modules, ActionRepository actions, PermissionRepository permissions) {
        this.modules     = modules;
        this.actions     = actions;
        this.permissions = permissions;
    }

    @GetMapping("/modules")     public List<Module>     modules()     { return modules.findAll(); }
    @GetMapping("/actions")     public List<Action>     actions()     { return actions.findAll(); }
    @GetMapping("/permissions") public List<Permission> permissions() { return permissions.findAll(); }
}
