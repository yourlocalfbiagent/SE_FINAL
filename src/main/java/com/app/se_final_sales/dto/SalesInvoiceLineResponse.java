package com.app.se_final_sales.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceLineResponse {
    private Long lineId;
    private Long productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
