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
public class SalesOrderRequest {
    @NotNull(message = "Partner ID is required")
    private Long partnerId;

    @NotNull(message = "Creator User ID is required")
    private Long createdById;

    private LocalDate orderDate;

    @NotBlank(message = "Status is required")
    private String status;

    @NotEmpty(message = "At least one order line is required")
    private List<SalesOrderLineRequest> lines;
}
