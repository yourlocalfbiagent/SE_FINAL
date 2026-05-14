package com.app.se_final_sales.controller;

import com.app.se_final_sales.dto.PaymentRequest;
import com.app.se_final_sales.dto.PaymentResponse;
import com.app.se_final_sales.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Record and retrieve invoice payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Record a payment against an invoice")
    public ResponseEntity<PaymentResponse> recordPayment(@Valid @RequestBody PaymentRequest request) {
        return new ResponseEntity<>(paymentService.recordPayment(request), HttpStatus.CREATED);
    }

    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "List all payments for an invoice")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByInvoiceId(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getPaymentsByInvoiceId(invoiceId));
    }
}
