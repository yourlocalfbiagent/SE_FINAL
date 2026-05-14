package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reorder_suggestions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReorderSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggestion_id")
    private Long suggestionId;

    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "current_available_quantity", precision = 19, scale = 4)
    private BigDecimal currentAvailableQuantity;

    @Column(name = "reorder_level_snapshot", precision = 19, scale = 4)
    private BigDecimal reorderLevelSnapshot;

    @Column(name = "suggested_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal suggestedQuantity;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "suggested_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime suggestedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
