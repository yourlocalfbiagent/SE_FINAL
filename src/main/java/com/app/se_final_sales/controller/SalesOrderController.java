package com.app.se_final_sales.controller;

import com.app.se_final_sales.dto.SalesOrderRequest;
import com.app.se_final_sales.dto.SalesOrderResponse;
import com.app.se_final_sales.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
@Tag(name = "Sales Orders", description = "Create and manage sales orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    @Operation(summary = "Create a new sales order")
    public ResponseEntity<SalesOrderResponse> createOrder(@Valid @RequestBody SalesOrderRequest request) {
        return new ResponseEntity<>(salesOrderService.createOrder(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sales order by ID")
    public ResponseEntity<SalesOrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getOrderById(id));
    }

    @GetMapping
    @Operation(summary = "List all sales orders")
    public ResponseEntity<List<SalesOrderResponse>> getAllOrders() {
        return ResponseEntity.ok(salesOrderService.getAllOrders());
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm a sales order")
    public ResponseEntity<SalesOrderResponse> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.confirmOrder(id));
    }
}
