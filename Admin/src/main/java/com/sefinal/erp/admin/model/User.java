package com.sefinal.erp.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

public record User(
        Integer userId,
        String firstName,
        String lastName,
        String email,
        @JsonIgnore String passwordHash,
        Integer companyId,
        Integer roleId,
        boolean isActive,
        boolean mfaEnabled,
        int failedLoginAttempts,
        LocalDateTime lockedUntil,
        LocalDateTime createdAt
) {}
