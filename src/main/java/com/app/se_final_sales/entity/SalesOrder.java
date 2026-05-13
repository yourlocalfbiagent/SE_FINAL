package com.app.se_final_sales.entity;

import com.sefinal.erp.entity.BusinessPartner;
import com.sefinal.erp.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SALES_ORDERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salesOrderId;

    @Column(unique = true, nullable = false, length = 100)
    private String salesOrderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private BusinessPartner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Builder.Default
    private LocalDate orderDate = LocalDate.now();

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 50)
    private String status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrderLine> lines = new ArrayList<>();
}
