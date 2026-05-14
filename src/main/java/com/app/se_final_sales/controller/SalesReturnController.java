package com.app.se_final_sales.controller;

import com.app.se_final_sales.dto.SalesReturnRequest;
import com.app.se_final_sales.dto.SalesReturnResponse;
import com.app.se_final_sales.service.SalesReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-returns")
@RequiredArgsConstructor
@Tag(name = "Sales Returns", description = "Process and manage sales returns")
public class SalesReturnController {

    private final SalesReturnService salesReturnService;

    @PostMapping
    @Operation(summary = "Process a new sales return")
    public ResponseEntity<SalesReturnResponse> processReturn(@Valid @RequestBody SalesReturnRequest request) {
        return new ResponseEntity<>(salesReturnService.processReturn(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sales return by ID")
    public ResponseEntity<SalesReturnResponse> getReturnById(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.getReturnById(id));
    }

    @GetMapping
    @Operation(summary = "List all sales returns")
    public ResponseEntity<List<SalesReturnResponse>> getAllReturns() {
        return ResponseEntity.ok(salesReturnService.getAllReturns());
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a sales return")
    public ResponseEntity<SalesReturnResponse> approveReturn(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.approveReturn(id));
    }
}
