package com.sefinal.erp.admin.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_exports")
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

    public ReportExport() {}

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

    public Integer getExportId() { return exportId; }
    public void setExportId(Integer exportId) { this.exportId = exportId; }

    public Integer getCompanyId() { return companyId; }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }

    public Integer getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(Integer generatedBy) { this.generatedBy = generatedBy; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }

    public Integer getRowCount() { return rowCount; }
    public void setRowCount(Integer rowCount) { this.rowCount = rowCount; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }

    public String getGeneratedByEmail() { return generatedByEmail; }
    public void setGeneratedByEmail(String generatedByEmail) { this.generatedByEmail = generatedByEmail; }
}
