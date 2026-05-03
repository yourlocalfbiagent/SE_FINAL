// StockMovement.java
package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_movements")
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Integer movementId;
}