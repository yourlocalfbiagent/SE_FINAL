package com.sefinal.erp.purchasinginventory.web;

import com.sefinal.erp.purchasinginventory.dao.PurchaseOrderDao;
import com.sefinal.erp.purchasinginventory.model.PurchaseOrder;
import com.sefinal.erp.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@Tag(name = "Purchase Orders", description = "Create and manage purchase orders")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.read')")
public class PurchaseOrderController {

    private final PurchaseOrderDao purchaseOrderDao;

    public PurchaseOrderController(PurchaseOrderDao purchaseOrderDao) {
        this.purchaseOrderDao = purchaseOrderDao;
    }

    @GetMapping
    @Operation(summary = "List all purchase orders")
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderDao.findByCompanyId(SecurityUtils.getCompanyId());
    }

    @PostMapping
    @Operation(summary = "Create a new purchase order")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.create')")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestBody PurchaseOrder purchaseOrder) {
        purchaseOrder.setCompanyId(SecurityUtils.getCompanyId());
        // supplier_id mirrors partner_id — both columns are NOT NULL in the DB
        if (purchaseOrder.getSupplierId() == null) purchaseOrder.setSupplierId(purchaseOrder.getPartnerId());
        if (purchaseOrder.getCreatedAt() == null) purchaseOrder.setCreatedAt(LocalDateTime.now());
        if (purchaseOrder.getStatus() == null || purchaseOrder.getStatus().isBlank()) purchaseOrder.setStatus("pending");
        
        if (purchaseOrder.getLines() != null) {
            BigDecimal total = BigDecimal.ZERO;
            for (var line : purchaseOrder.getLines()) {
                if (line.getQuantityOrdered() == null) line.setQuantityOrdered(BigDecimal.ZERO);
                if (line.getUnitCost() == null) line.setUnitCost(BigDecimal.ZERO);
                line.setLineTotal(line.getUnitCost().multiply(line.getQuantityOrdered()));
                total = total.add(line.getLineTotal());
            }
            purchaseOrder.setTotalAmount(total);
        } else if (purchaseOrder.getTotalAmount() == null) {
            purchaseOrder.setTotalAmount(BigDecimal.ZERO);
        }

        if (purchaseOrder.getOrderDate() == null) purchaseOrder.setOrderDate(LocalDate.now());
        PurchaseOrder saved = purchaseOrderDao.save(purchaseOrder);
        return ResponseEntity.created(URI.create("/api/purchase-orders/" + saved.getPoId())).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a purchase order")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.update')")
    public ResponseEntity<PurchaseOrder> updatePurchaseOrder(@PathVariable Long id, @RequestBody PurchaseOrder purchaseOrder) {
        if (!purchaseOrderDao.existsById(id)) return ResponseEntity.notFound().build();
        purchaseOrder.setPoId(id);
        purchaseOrder.setCompanyId(SecurityUtils.getCompanyId());
        if (purchaseOrder.getSupplierId() == null) purchaseOrder.setSupplierId(purchaseOrder.getPartnerId());
        
        if (purchaseOrder.getLines() != null) {
            BigDecimal total = BigDecimal.ZERO;
            for (var line : purchaseOrder.getLines()) {
                if (line.getQuantityOrdered() == null) line.setQuantityOrdered(BigDecimal.ZERO);
                if (line.getUnitCost() == null) line.setUnitCost(BigDecimal.ZERO);
                line.setLineTotal(line.getUnitCost().multiply(line.getQuantityOrdered()));
                total = total.add(line.getLineTotal());
            }
            purchaseOrder.setTotalAmount(total);
        } else if (purchaseOrder.getTotalAmount() == null) {
            purchaseOrder.setTotalAmount(BigDecimal.ZERO);
        }

        return ResponseEntity.ok(purchaseOrderDao.save(purchaseOrder));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a purchase order")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.delete')")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long id) {
        if (!purchaseOrderDao.existsById(id)) return ResponseEntity.notFound().build();
        purchaseOrderDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
