package com.app.se_final_sales.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResponse {
    private Long approvalId;
    private Long invoiceId;
    private String invoiceNumber;
    private Long requestedById;
    private String requestedByEmail;
    private Long reviewedById;
    private String reviewedByEmail;
    private String status;
    private String comments;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
}
