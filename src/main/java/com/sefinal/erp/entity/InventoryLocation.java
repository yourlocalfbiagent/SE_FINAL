package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory_locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "location_name", nullable = false, length = 100)
    private String locationName;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Builder.Default
    @Column(name = "quantity_on_hand", precision = 19, scale = 4)
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "quantity_reserved", precision = 19, scale = 4)
    private BigDecimal quantityReserved = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "quantity_available", precision = 19, scale = 4)
    private BigDecimal quantityAvailable = BigDecimal.ZERO;
}
