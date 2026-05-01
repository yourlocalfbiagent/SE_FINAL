package com.app.se_final_sales.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderResponse {
    private Long salesOrderId;
    private String salesOrderNumber;
    private Long partnerId;
    private String partnerName;
    private Long createdById;
    private String createdByEmail;
    private LocalDate orderDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<SalesOrderLineResponse> lines;
}
