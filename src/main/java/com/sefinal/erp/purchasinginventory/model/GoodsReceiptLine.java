package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "goods_receipt_lines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GoodsReceiptLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_line_id")
    private Long receiptLineId;

    @Column(name = "receipt_id", nullable = false)
    private Long receiptId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity_received", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityReceived;
}
