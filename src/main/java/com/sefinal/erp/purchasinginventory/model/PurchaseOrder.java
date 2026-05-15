package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "po_id")
    private Long poId;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "po_number", unique = true, nullable = false, length = 50)
    private String poNumber;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "pending";

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "total_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "po_id")
    private java.util.List<PurchaseOrderLine> lines;
}
