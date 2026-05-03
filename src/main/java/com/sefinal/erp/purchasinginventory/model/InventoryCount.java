// InventoryCount.java
package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_counts")
public class InventoryCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "count_id")
    private Integer countId;
}