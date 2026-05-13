// GoodsReceiptLine.java
package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "goods_receipt_lines")
public class GoodsReceiptLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_line_id")
    private Integer receiptLineId;
}