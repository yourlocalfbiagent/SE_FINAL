package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_imports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BulkImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "import_id")
    private Long importId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "imported_by")
    private Long importedBy;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(name = "total_rows")
    @Builder.Default
    private Integer totalRows = 0;

    @Column(name = "successful_rows")
    @Builder.Default
    private Integer successfulRows = 0;

    @Column(name = "failed_rows")
    @Builder.Default
    private Integer failedRows = 0;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "imported_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime importedAt = LocalDateTime.now();
}
