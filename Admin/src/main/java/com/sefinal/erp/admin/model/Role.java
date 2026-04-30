package com.sefinal.erp.admin.model;

public record Role(
        Integer roleId,
        String roleName,
        String description,
        Integer companyId,
        boolean isActive
) {}
