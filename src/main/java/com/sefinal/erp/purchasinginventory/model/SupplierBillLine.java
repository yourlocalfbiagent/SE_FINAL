package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "supplier_bill_lines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupplierBillLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_line_id")
    private Long billLineId;

    @Column(name = "bill_id", nullable = false)
    private Long billId;

    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal;
}
