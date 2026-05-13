package com.erp.mainmodule.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bulk_import_errors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkImportError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "error_id")
    private Integer errorId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_id", nullable = false)
    private BulkImport bulkImport;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "field_name", length = 255)
    private String fieldName;

    @Column(name = "error_message", nullable = false, length = 1000)
    private String errorMessage;
}

