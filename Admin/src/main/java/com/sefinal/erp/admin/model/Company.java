package com.sefinal.erp.admin.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Company(
        Integer companyId,
        String companyName,
        String currency,
        BigDecimal taxDefault,
        String locale,
        boolean isActive,
        LocalDateTime createdAt
) {}
