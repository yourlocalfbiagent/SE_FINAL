package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.dao.ActionDao;
import com.sefinal.erp.admin.dao.ModuleDao;
import com.sefinal.erp.admin.dao.PermissionDao;
import com.sefinal.erp.admin.model.Action;
import com.sefinal.erp.admin.model.Module;
import com.sefinal.erp.admin.model.Permission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final ModuleDao modules;
    private final ActionDao actions;
    private final PermissionDao permissions;

    public CatalogController(ModuleDao modules, ActionDao actions, PermissionDao permissions) {
        this.modules = modules;
        this.actions = actions;
        this.permissions = permissions;
    }

    @GetMapping("/modules")     public List<Module>     modules()     { return modules.findAll(); }
    @GetMapping("/actions")     public List<Action>     actions()     { return actions.findAll(); }
    @GetMapping("/permissions") public List<Permission> permissions() { return permissions.findAll(); }
}
