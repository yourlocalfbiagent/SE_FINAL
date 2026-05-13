package com.app.se_final_sales.service;

import com.app.se_final_sales.dto.ApprovalRequestDTO;
import com.app.se_final_sales.dto.ApprovalResponse;
import com.app.se_final_sales.dto.ApprovalReviewDTO;

import java.util.List;

public interface ApprovalService {
    ApprovalResponse submitForApproval(ApprovalRequestDTO request);
    ApprovalResponse reviewApproval(Long id, ApprovalReviewDTO review);
    ApprovalResponse getApprovalById(Long id);
    List<ApprovalResponse> getApprovalsByInvoiceId(Long invoiceId);
}
