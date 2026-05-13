package com.app.se_final_sales.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(unique = true, nullable = false, length = 100)
    private String sku;

    @Column(length = 50)
    private String unitOfMeasure;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal costPrice;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal sellingPrice;

    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    private Long categoryId; // References external PRODUCT_CATEGORIES

    private Long companyId;  // References external COMPANIES

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
