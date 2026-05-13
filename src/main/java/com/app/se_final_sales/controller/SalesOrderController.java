package com.app.se_final_sales.controller;

import com.app.se_final_sales.dto.SalesOrderRequest;
import com.app.se_final_sales.dto.SalesOrderResponse;
import com.app.se_final_sales.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    public ResponseEntity<SalesOrderResponse> createOrder(@Valid @RequestBody SalesOrderRequest request) {
        return new ResponseEntity<>(salesOrderService.createOrder(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<SalesOrderResponse>> getAllOrders() {
        return ResponseEntity.ok(salesOrderService.getAllOrders());
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<SalesOrderResponse> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.confirmOrder(id));
    }
}
