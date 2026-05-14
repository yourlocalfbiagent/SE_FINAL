package com.sefinal.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", unique = true, nullable = false, length = 100)
    private String categoryName;

    @Column(name = "parent_category_id")
    private Long parentCategoryId;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "company_id")
    private Long companyId;
}
