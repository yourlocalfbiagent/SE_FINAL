package com.app.se_final_sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReturnRequest {
    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    @NotNull(message = "Processor User ID is required")
    private Long processedById;

    private LocalDate returnDate;
    private String reason;

    @NotBlank(message = "Status is required")
    private String status;

    @NotEmpty(message = "At least one return line is required")
    private List<SalesReturnLineRequest> lines;
}
