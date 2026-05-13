package com.app.se_final_sales.controller;

import com.app.se_final_sales.dto.ApprovalRequestDTO;
import com.app.se_final_sales.dto.ApprovalResponse;
import com.app.se_final_sales.dto.ApprovalReviewDTO;
import com.app.se_final_sales.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<ApprovalResponse> submitForApproval(@Valid @RequestBody ApprovalRequestDTO request) {
        return new ResponseEntity<>(approvalService.submitForApproval(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<ApprovalResponse> reviewApproval(@PathVariable Long id, @Valid @RequestBody ApprovalReviewDTO review) {
        return ResponseEntity.ok(approvalService.reviewApproval(id, review));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalResponse> getApprovalById(@PathVariable Long id) {
        return ResponseEntity.ok(approvalService.getApprovalById(id));
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<ApprovalResponse>> getApprovalsByInvoiceId(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(approvalService.getApprovalsByInvoiceId(invoiceId));
    }
}
