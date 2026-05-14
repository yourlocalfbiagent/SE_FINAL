package com.app.se_final_sales.controller;

import com.app.se_final_sales.dto.SalesInvoiceRequest;
import com.app.se_final_sales.dto.SalesInvoiceResponse;
import com.app.se_final_sales.service.SalesInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-invoices")
@RequiredArgsConstructor
@Tag(name = "Sales Invoices", description = "Create and manage sales invoices")
public class SalesInvoiceController {

    private final SalesInvoiceService salesInvoiceService;

    @PostMapping
    @Operation(summary = "Create a new sales invoice")
    public ResponseEntity<SalesInvoiceResponse> createInvoice(@Valid @RequestBody SalesInvoiceRequest request) {
        return new ResponseEntity<>(salesInvoiceService.createInvoice(request), HttpStatus.CREATED);
    }

    @PostMapping("/generate-from-order/{orderId}")
    @Operation(summary = "Generate an invoice from an existing sales order")
    public ResponseEntity<SalesInvoiceResponse> generateFromOrder(@PathVariable Long orderId) {
        return new ResponseEntity<>(salesInvoiceService.generateInvoiceFromOrder(orderId), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sales invoice by ID")
    public ResponseEntity<SalesInvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(salesInvoiceService.getInvoiceById(id));
    }

    @GetMapping
    @Operation(summary = "List all sales invoices")
    public ResponseEntity<List<SalesInvoiceResponse>> getAllInvoices() {
        return ResponseEntity.ok(salesInvoiceService.getAllInvoices());
    }
}
