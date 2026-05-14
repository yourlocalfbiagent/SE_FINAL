package com.app.se_final_sales.controller;

import com.app.se_final_sales.dto.SalesOrderRequest;
import com.app.se_final_sales.dto.SalesOrderResponse;
import com.app.se_final_sales.service.SalesOrderService;
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
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
@Tag(name = "Sales Orders", description = "Create and manage sales orders")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.read')")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    @Operation(summary = "Create a new sales order")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.create')")
    public ResponseEntity<SalesOrderResponse> createOrder(@Valid @RequestBody SalesOrderRequest request) {
        return new ResponseEntity<>(salesOrderService.createOrder(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sales order by ID")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.read')")
    public ResponseEntity<SalesOrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getOrderById(id));
    }

    @GetMapping
    @Operation(summary = "List all sales orders")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.read')")
    public ResponseEntity<List<SalesOrderResponse>> getAllOrders() {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long companyId = ((Number) claims.get("companyId")).longValue();
        return ResponseEntity.ok(salesOrderService.getAllOrders(companyId));
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm a sales order")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.update')")
    public ResponseEntity<SalesOrderResponse> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.confirmOrder(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a sales order")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.update')")
    public ResponseEntity<SalesOrderResponse> updateOrder(@PathVariable Long id, @Valid @RequestBody SalesOrderRequest request) {
        return ResponseEntity.ok(salesOrderService.updateOrder(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sales order")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.delete')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        salesOrderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
