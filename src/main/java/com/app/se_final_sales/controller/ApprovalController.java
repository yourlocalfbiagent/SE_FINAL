package com.app.se_final_sales.controller;

import com.app.se_final_sales.dto.ApprovalRequestDTO;
import com.app.se_final_sales.dto.ApprovalResponse;
import com.app.se_final_sales.dto.ApprovalReviewDTO;
import com.app.se_final_sales.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Tag(name = "Approvals", description = "Invoice approval workflow")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.read')")
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping
    @Operation(summary = "Submit an invoice for approval")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.update')")
    public ResponseEntity<ApprovalResponse> submitForApproval(@Valid @RequestBody ApprovalRequestDTO request) {
        return new ResponseEntity<>(approvalService.submitForApproval(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/review")
    @Operation(summary = "Approve or reject an approval request")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.approve')")
    public ResponseEntity<ApprovalResponse> reviewApproval(@PathVariable Long id, @Valid @RequestBody ApprovalReviewDTO review) {
        return ResponseEntity.ok(approvalService.reviewApproval(id, review));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get approval request by ID")
    public ResponseEntity<ApprovalResponse> getApprovalById(@PathVariable Long id) {
        return ResponseEntity.ok(approvalService.getApprovalById(id));
    }

    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "List all approvals for an invoice")
    public ResponseEntity<List<ApprovalResponse>> getApprovalsByInvoiceId(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(approvalService.getApprovalsByInvoiceId(invoiceId));
    }
}
