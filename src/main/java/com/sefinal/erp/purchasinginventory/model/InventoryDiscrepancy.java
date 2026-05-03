// InventoryDiscrepancy.java
package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_discrepancies")
public class InventoryDiscrepancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discrepancy_id")
    private Integer discrepancyId;
}