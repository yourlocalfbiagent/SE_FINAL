package com.app.se_final_sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalReviewDTO {
    @NotNull(message = "Reviewer User ID is required")
    private Long reviewedById;

    @NotBlank(message = "Status is required (APPROVED/REJECTED)")
    private String status;

    private String comments;
}
