package com.app.se_final_sales.controller;

import com.app.se_final_sales.dto.SalesInvoiceRequest;
import com.app.se_final_sales.dto.SalesInvoiceResponse;
import com.app.se_final_sales.service.SalesInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-invoices")
@RequiredArgsConstructor
public class SalesInvoiceController {

    private final SalesInvoiceService salesInvoiceService;

    @PostMapping
    public ResponseEntity<SalesInvoiceResponse> createInvoice(@Valid @RequestBody SalesInvoiceRequest request) {
        return new ResponseEntity<>(salesInvoiceService.createInvoice(request), HttpStatus.CREATED);
    }

    @PostMapping("/generate-from-order/{orderId}")
    public ResponseEntity<SalesInvoiceResponse> generateFromOrder(@PathVariable Long orderId) {
        return new ResponseEntity<>(salesInvoiceService.generateInvoiceFromOrder(orderId), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesInvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(salesInvoiceService.getInvoiceById(id));
    }

    @GetMapping
    public ResponseEntity<List<SalesInvoiceResponse>> getAllInvoices() {
        return ResponseEntity.ok(salesInvoiceService.getAllInvoices());
    }
}
