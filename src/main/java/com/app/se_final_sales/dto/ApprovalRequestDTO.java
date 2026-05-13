package com.app.se_final_sales.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestDTO {
    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    @NotNull(message = "Requester User ID is required")
    private Long requestedById;
}
