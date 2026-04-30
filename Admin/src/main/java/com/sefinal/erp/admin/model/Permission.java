package com.sefinal.erp.admin.model;

public record Permission(
        Integer permissionId,
        Integer moduleId,
        String moduleName,
        Integer actionId,
        String actionName
) {}
