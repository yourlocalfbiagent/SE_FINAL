package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "low_stock_alerts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LowStockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity_at_alert", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityAtAlert;

    @Column(name = "reorder_level", nullable = false, precision = 19, scale = 4)
    private BigDecimal reorderLevel;

    @Builder.Default
    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "triggered_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime triggeredAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
