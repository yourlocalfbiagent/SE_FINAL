package com.sefinal.erp.admin.model;

public record Module(
        Integer moduleId,
        String moduleName,
        boolean isActive
) {}
