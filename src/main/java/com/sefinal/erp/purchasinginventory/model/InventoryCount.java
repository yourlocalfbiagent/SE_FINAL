package com.sefinal.erp.purchasinginventory.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_counts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "count_id")
    private Long countId;

    @Column(name = "count_number", unique = true, nullable = false, length = 50)
    private String countNumber;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "counted_by")
    private Long countedBy;

    @Column(name = "count_date", nullable = false)
    private LocalDate countDate;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "draft";

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "inventoryCount", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<InventoryCountLine> lines;
}
