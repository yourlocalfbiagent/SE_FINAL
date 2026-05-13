package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_exports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportExport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "export_id")
    private Long exportId;

    @Column(name = "generated_by")
    private Long generatedBy;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "file_format", nullable = false, length = 10)
    private String fileFormat;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "generated_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();
}
