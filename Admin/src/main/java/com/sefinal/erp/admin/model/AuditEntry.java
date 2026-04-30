package com.sefinal.erp.admin.model;

import java.time.LocalDateTime;

public record AuditEntry(
        Long auditId,
        Integer userId,
        String userEmail,
        Integer companyId,
        String action,
        String entityType,
        Integer entityId,
        String details,
        LocalDateTime createdAt
) {}
