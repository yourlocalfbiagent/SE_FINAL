package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.model.Action;
import com.sefinal.erp.admin.model.Module;
import com.sefinal.erp.admin.model.Permission;
import com.sefinal.erp.admin.repository.ActionRepository;
import com.sefinal.erp.admin.repository.ModuleRepository;
import com.sefinal.erp.admin.repository.PermissionRepository;
import com.sefinal.erp.admin.web.dto.Dtos.ActionRequest;
import com.sefinal.erp.admin.web.dto.Dtos.CreatePermissionRequest;
import com.sefinal.erp.admin.web.dto.Dtos.ModuleRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.read')")
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

    @PostMapping("/modules")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.create')")
    public Module createModule(@RequestBody ModuleRequest req) {
        Module m = new Module();
        m.setModuleName(req.moduleName());
        m.setDescription(req.description());
        m.setActive(req.isActive() == null || req.isActive());
        return modules.save(m);
    }

    @PostMapping("/actions")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.create')")
    public Action createAction(@RequestBody ActionRequest req) {
        Action a = new Action();
        a.setActionName(req.actionName());
        a.setDescription(req.description());
        return actions.save(a);
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN.create')")
    public Permission createPermission(@RequestBody CreatePermissionRequest req) {
        Module m = modules.findByModuleName(req.moduleName())
                .orElseGet(() -> {
                    Module newMod = new Module();
                    newMod.setModuleName(req.moduleName());
                    newMod.setActive(true);
                    return modules.save(newMod);
                });

        Action a = actions.findByActionName(req.actionName())
                .orElseGet(() -> {
                    Action newAct = new Action();
                    newAct.setActionName(req.actionName());
                    return actions.save(newAct);
                });

        Permission p = new Permission();
        p.setModule(m);
        p.setAction(a);
        p.setDescription(req.description());
        return permissions.save(p);
    }
}
