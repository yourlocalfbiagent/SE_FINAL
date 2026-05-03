// SupplierBillLine.java
package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "supplier_bill_lines")
public class SupplierBillLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_line_id")
    private Integer billLineId;
}