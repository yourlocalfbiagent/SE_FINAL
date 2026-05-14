package com.app.se_final_sales.controller;

import com.app.se_final_sales.dto.PaymentRequest;
import com.app.se_final_sales.dto.PaymentResponse;
import com.app.se_final_sales.service.PaymentService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Record and retrieve invoice payments")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.read')")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Record a payment against an invoice")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.create')")
    public ResponseEntity<PaymentResponse> recordPayment(@Valid @RequestBody PaymentRequest request) {
        return new ResponseEntity<>(paymentService.recordPayment(request), HttpStatus.CREATED);
    }

    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "Get payments by invoice ID")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByInvoiceId(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getPaymentsByInvoiceId(invoiceId));
    }

    @GetMapping
    @Operation(summary = "List all payments")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long companyId = ((Number) claims.get("companyId")).longValue();
        return ResponseEntity.ok(paymentService.getAllPayments(companyId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a payment")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.update')")
    public ResponseEntity<PaymentResponse> updatePayment(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.updatePayment(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a payment")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.delete')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
    }
