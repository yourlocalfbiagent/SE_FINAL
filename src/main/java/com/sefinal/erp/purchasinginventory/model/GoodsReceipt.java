package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goods_receipts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GoodsReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "po_id")
    private Long poId;

    @Column(name = "received_by")
    private Long receivedBy;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
