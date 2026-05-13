package com.app.se_final_sales.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SALES_RETURNS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReturn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnId;

    @Column(unique = true, nullable = false, length = 100)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private SalesInvoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @Builder.Default
    private LocalDate returnDate = LocalDate.now();

    private String reason;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 50)
    private String status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "salesReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesReturnLine> lines = new ArrayList<>();
}
