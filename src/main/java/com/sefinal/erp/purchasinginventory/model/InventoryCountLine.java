package com.sefinal.erp.purchasinginventory.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory_count_lines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryCountLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "count_line_id")
    private Long countLineId;

    @Column(name = "count_id", nullable = false, insertable = false, updatable = false)
    private Long countId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "count_id", nullable = false)
    private InventoryCount inventoryCount;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "system_quantity", precision = 19, scale = 4)
    private BigDecimal systemQuantity;

    @Column(name = "counted_quantity", precision = 19, scale = 4)
    private BigDecimal countedQuantity;

    @Column(name = "variance_quantity", precision = 19, scale = 4)
    private BigDecimal varianceQuantity;
}
