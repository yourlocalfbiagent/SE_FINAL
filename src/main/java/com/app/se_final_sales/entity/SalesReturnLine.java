package com.app.se_final_sales.entity;

import com.sefinal.erp.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "SALES_RETURN_LINES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReturnLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id")
    private SalesReturn salesReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal;
}
