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
public class SalesReturnResponse {
    private Long returnId;
    private String returnNumber;
    private Long invoiceId;
    private String invoiceNumber;
    private Long processedById;
    private String processedByEmail;
    private LocalDate returnDate;
    private String reason;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<SalesReturnLineResponse> lines;
}
