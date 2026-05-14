package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bulk_import_errors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BulkImportError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "error_id")
    private Long errorId;

    @Column(name = "import_id", nullable = false)
    private Long importId;

    @Column(name = "row_number")
    private Integer rowNumber;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "error_message", nullable = false, length = 1000)
    private String errorMessage;
}
