package com.sefinal.erp.purchasinginventory.web;

import com.sefinal.erp.purchasinginventory.dao.InventoryCountDao;
import com.sefinal.erp.purchasinginventory.dao.StockMovementDao;
import com.sefinal.erp.purchasinginventory.model.InventoryCount;
import com.sefinal.erp.purchasinginventory.model.StockMovement;
import com.sefinal.erp.security.SecurityUtils;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Inventory counts and stock movements")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('INVENTORY.read')")
public class InventoryController {

    private final InventoryCountDao inventoryCountDao;
    private final StockMovementDao stockMovementDao;

    public InventoryController(InventoryCountDao inventoryCountDao, StockMovementDao stockMovementDao) {
        this.inventoryCountDao = inventoryCountDao;
        this.stockMovementDao = stockMovementDao;
    }

    @GetMapping("/counts")
    @Operation(summary = "List all inventory counts")
    public List<InventoryCount> getAllInventoryCounts() {
        return inventoryCountDao.findByCompanyId(SecurityUtils.getCompanyId());
    }

    @GetMapping("/stock-movements")
    @Operation(summary = "List all stock movements")
    public List<StockMovement> getAllStockMovements() {
        return stockMovementDao.findByCompanyId(SecurityUtils.getCompanyId());
    }

    @PostMapping("/counts")
    @Operation(summary = "Create a new inventory count")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('INVENTORY.create')")
    public InventoryCount createInventoryCount(@RequestBody InventoryCount inventoryCount) {
        inventoryCount.setCompanyId(SecurityUtils.getCompanyId());
        return inventoryCountDao.save(inventoryCount);
    }

    @PutMapping("/counts/{id}")
    @Operation(summary = "Update an inventory count")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('INVENTORY.update')")
    public ResponseEntity<InventoryCount> updateInventoryCount(@PathVariable Long id, @RequestBody InventoryCount inventoryCount) {
        if (!inventoryCountDao.existsById(id)) return ResponseEntity.notFound().build();
        inventoryCount.setCountId(id);
        inventoryCount.setCompanyId(SecurityUtils.getCompanyId());
        return ResponseEntity.ok(inventoryCountDao.save(inventoryCount));
    }

    @DeleteMapping("/counts/{id}")
    @Operation(summary = "Delete an inventory count")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('INVENTORY.delete')")
    public ResponseEntity<Void> deleteInventoryCount(@PathVariable Long id) {
        if (!inventoryCountDao.existsById(id)) return ResponseEntity.notFound().build();
        inventoryCountDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/stock-movements")
    @Operation(summary = "Record a new stock movement")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('INVENTORY.create')")
    public StockMovement createStockMovement(@RequestBody StockMovement stockMovement) {
        stockMovement.setCompanyId(SecurityUtils.getCompanyId());
        if (stockMovement.getMovementDate() == null) stockMovement.setMovementDate(java.time.LocalDateTime.now());
        if (stockMovement.getMovementType() == null || stockMovement.getMovementType().isBlank()) {
            BigDecimal qc = stockMovement.getQuantityChange();
            stockMovement.setMovementType(qc != null && qc.compareTo(BigDecimal.ZERO) >= 0 ? "IN" : "OUT");
        }
        if (stockMovement.getQuantity() == null)
            stockMovement.setQuantity(stockMovement.getQuantityChange() != null
                ? stockMovement.getQuantityChange().abs() : BigDecimal.ZERO);
        return stockMovementDao.save(stockMovement);
    }

    @PutMapping("/stock-movements/{id}")
    @Operation(summary = "Update a stock movement")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('INVENTORY.update')")
    public ResponseEntity<StockMovement> updateStockMovement(@PathVariable Long id, @RequestBody StockMovement stockMovement) {
        if (!stockMovementDao.existsById(id)) return ResponseEntity.notFound().build();
        stockMovement.setMovementId(id);
        stockMovement.setCompanyId(SecurityUtils.getCompanyId());
        return ResponseEntity.ok(stockMovementDao.save(stockMovement));
    }

    @DeleteMapping("/stock-movements/{id}")
    @Operation(summary = "Delete a stock movement")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('INVENTORY.delete')")
    public ResponseEntity<Void> deleteStockMovement(@PathVariable Long id) {
        if (!stockMovementDao.existsById(id)) return ResponseEntity.notFound().build();
        stockMovementDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
