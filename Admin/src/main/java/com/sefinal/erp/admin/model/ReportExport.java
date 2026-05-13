package com.sefinal.erp.admin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_exports")
@Getter @Setter @NoArgsConstructor
public class ReportExport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "export_id")
    private Integer exportId;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    @Column(name = "generated_by")
    private Integer generatedBy;

    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "file_format", nullable = false)
    private String fileFormat;

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "generated_at", insertable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Transient
    private String generatedByEmail;

    public ReportExport(Integer companyId, Integer generatedBy, String reportType,
                        LocalDate periodStart, LocalDate periodEnd, String fileFormat, Integer rowCount) {
        this.companyId   = companyId;
        this.generatedBy = generatedBy;
        this.reportType  = reportType;
        this.periodStart = periodStart;
        this.periodEnd   = periodEnd;
        this.fileFormat  = fileFormat;
        this.rowCount    = rowCount;
    }
}
