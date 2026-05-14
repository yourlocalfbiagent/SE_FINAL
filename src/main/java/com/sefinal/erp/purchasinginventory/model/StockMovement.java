package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long movementId;

    @Column(name = "movement_date", nullable = false)
    private java.time.LocalDateTime movementDate;

    @Column(name = "movement_type", nullable = false, length = 20)
    private String movementType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "goods_receipt_line_id")
    private Long goodsReceiptLineId;

    @Column(name = "sales_invoice_line_id")
    private Long salesInvoiceLineId;

    @Column(name = "sales_return_line_id")
    private Long salesReturnLineId;

    @Column(name = "discrepancy_id")
    private Long discrepancyId;

    @Column(name = "quantity_change", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityChange;

    @Column(name = "reason_code", nullable = false, length = 50)
    private String reasonCode;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "moved_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime movedAt = LocalDateTime.now();
}
