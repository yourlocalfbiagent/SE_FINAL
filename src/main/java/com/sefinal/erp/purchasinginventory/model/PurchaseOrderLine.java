// PurchaseOrderLine.java
package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "purchase_order_lines")
public class PurchaseOrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Integer lineId;
}