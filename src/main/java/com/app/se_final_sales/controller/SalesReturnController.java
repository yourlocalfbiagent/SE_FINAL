package com.app.se_final_sales.controller;

import com.app.se_final_sales.dto.SalesReturnRequest;
import com.app.se_final_sales.dto.SalesReturnResponse;
import com.app.se_final_sales.service.SalesReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-returns")
@RequiredArgsConstructor
public class SalesReturnController {

    private final SalesReturnService salesReturnService;

    @PostMapping
    public ResponseEntity<SalesReturnResponse> processReturn(@Valid @RequestBody SalesReturnRequest request) {
        return new ResponseEntity<>(salesReturnService.processReturn(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesReturnResponse> getReturnById(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.getReturnById(id));
    }

    @GetMapping
    public ResponseEntity<List<SalesReturnResponse>> getAllReturns() {
        return ResponseEntity.ok(salesReturnService.getAllReturns());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<SalesReturnResponse> approveReturn(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.approveReturn(id));
    }
}
