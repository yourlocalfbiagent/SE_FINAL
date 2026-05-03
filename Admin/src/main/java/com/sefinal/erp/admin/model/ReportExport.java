package com.sefinal.erp.admin.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReportExport(
        int exportId,
        int companyId,
        Integer generatedBy,
        String generatedByEmail,
        String reportType,
        LocalDate periodStart,
        LocalDate periodEnd,
        String fileFormat,
        Integer rowCount,
        LocalDateTime generatedAt
) {}
