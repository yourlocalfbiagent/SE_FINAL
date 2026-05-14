package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_discrepancies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryDiscrepancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discrepancy_id")
    private Long discrepancyId;

    @Column(name = "count_line_id", nullable = false)
    private Long countLineId;

    @Column(name = "recorded_by")
    private Long recordedBy;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "system_quantity", precision = 19, scale = 4)
    private BigDecimal systemQuantity;

    @Column(name = "counted_quantity", precision = 19, scale = 4)
    private BigDecimal countedQuantity;

    @Column(name = "variance_quantity", precision = 19, scale = 4)
    private BigDecimal varianceQuantity;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "open";

    @Column(length = 500)
    private String notes;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
