package com.sefinal.erp.purchasinginventory.web;

import com.sefinal.erp.entity.InventoryLocation;
import com.sefinal.erp.repository.InventoryLocationRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Inventory counts and stock movements")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('INVENTORY.read')")
@Transactional
public class InventoryController {

    private final InventoryCountDao inventoryCountDao;
    private final StockMovementDao stockMovementDao;
    private final InventoryLocationRepository inventoryLocationRepository;

    public InventoryController(InventoryCountDao inventoryCountDao,
                               StockMovementDao stockMovementDao,
                               InventoryLocationRepository inventoryLocationRepository) {
        this.inventoryCountDao = inventoryCountDao;
        this.stockMovementDao = stockMovementDao;
        this.inventoryLocationRepository = inventoryLocationRepository;
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
        if (inventoryCount.getLines() != null) {
            for (var line : inventoryCount.getLines()) {
                line.setInventoryCount(inventoryCount);
                if (line.getSystemQuantity() == null) line.setSystemQuantity(BigDecimal.ZERO);
                if (line.getCountedQuantity() == null) line.setCountedQuantity(BigDecimal.ZERO);
                line.setVarianceQuantity(line.getCountedQuantity().subtract(line.getSystemQuantity()));
            }
        }
        return inventoryCountDao.save(inventoryCount);
    }

    @PutMapping("/counts/{id}")
    @Operation(summary = "Update an inventory count")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('INVENTORY.update')")
    public ResponseEntity<InventoryCount> updateInventoryCount(@PathVariable Long id, @RequestBody InventoryCount inventoryCount) {
        if (!inventoryCountDao.existsById(id)) return ResponseEntity.notFound().build();
        inventoryCount.setCountId(id);
        inventoryCount.setCompanyId(SecurityUtils.getCompanyId());
        if (inventoryCount.getLines() != null) {
            for (var line : inventoryCount.getLines()) {
                line.setInventoryCount(inventoryCount);
                if (line.getSystemQuantity() == null) line.setSystemQuantity(BigDecimal.ZERO);
                if (line.getCountedQuantity() == null) line.setCountedQuantity(BigDecimal.ZERO);
                line.setVarianceQuantity(line.getCountedQuantity().subtract(line.getSystemQuantity()));
            }
        }
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
        normalizeStockMovement(stockMovement);
        StockMovement saved = stockMovementDao.save(stockMovement);
        applyStockDelta(saved.getCompanyId(), saved.getProductId(), saved.getLocationId(), saved.getQuantityChange());
        return saved;
    }

    @PutMapping("/stock-movements/{id}")
    @Operation(summary = "Update a stock movement")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('INVENTORY.update')")
    public ResponseEntity<StockMovement> updateStockMovement(@PathVariable Long id, @RequestBody StockMovement stockMovement) {
        StockMovement existing = stockMovementDao.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        Long companyId = SecurityUtils.getCompanyId();
        if (existing.getCompanyId() != null && !existing.getCompanyId().equals(companyId)) {
            return ResponseEntity.notFound().build();
        }
        stockMovement.setMovementId(id);
        stockMovement.setCompanyId(companyId);
        normalizeStockMovement(stockMovement);
        StockMovement saved = stockMovementDao.save(stockMovement);

        applyStockDelta(
                existing.getCompanyId(),
                existing.getProductId(),
                existing.getLocationId(),
                safe(existing.getQuantityChange()).negate()
        );
        applyStockDelta(
                saved.getCompanyId(),
                saved.getProductId(),
                saved.getLocationId(),
                saved.getQuantityChange()
        );
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/stock-movements/{id}")
    @Operation(summary = "Delete a stock movement")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('INVENTORY.delete')")
    public ResponseEntity<Void> deleteStockMovement(@PathVariable Long id) {
        StockMovement existing = stockMovementDao.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        Long companyId = SecurityUtils.getCompanyId();
        if (existing.getCompanyId() != null && !existing.getCompanyId().equals(companyId)) {
            return ResponseEntity.notFound().build();
        }
        stockMovementDao.deleteById(id);
        applyStockDelta(
                existing.getCompanyId(),
                existing.getProductId(),
                existing.getLocationId(),
                safe(existing.getQuantityChange()).negate()
        );
        return ResponseEntity.noContent().build();
    }

    private void normalizeStockMovement(StockMovement stockMovement) {
        if (stockMovement.getMovementDate() == null) {
            stockMovement.setMovementDate(java.time.LocalDateTime.now());
        }
        if (stockMovement.getReasonCode() == null || stockMovement.getReasonCode().isBlank()) {
            stockMovement.setReasonCode("MANUAL");
        }
        if (stockMovement.getQuantityChange() == null) {
            BigDecimal quantity = safe(stockMovement.getQuantity());
            if ("OUT".equalsIgnoreCase(stockMovement.getMovementType())) {
                stockMovement.setQuantityChange(quantity.negate());
            } else {
                stockMovement.setQuantityChange(quantity);
            }
        }
        if (stockMovement.getMovementType() == null || stockMovement.getMovementType().isBlank()) {
            stockMovement.setMovementType(
                    stockMovement.getQuantityChange().compareTo(BigDecimal.ZERO) >= 0 ? "IN" : "OUT"
            );
        }
        if (stockMovement.getQuantity() == null) {
            stockMovement.setQuantity(stockMovement.getQuantityChange().abs());
        }
    }

    private void applyStockDelta(Long companyId, Long productId, Long locationId, BigDecimal quantityChange) {
        BigDecimal delta = safe(quantityChange);
        if (delta.compareTo(BigDecimal.ZERO) == 0) return;

        InventoryLocation location = resolveInventoryLocation(companyId, productId, locationId);
        BigDecimal updatedOnHand = safe(location.getQuantityOnHand()).add(delta);

        location.setQuantityOnHand(updatedOnHand);
        location.setQuantityAvailable(updatedOnHand.subtract(safe(location.getQuantityReserved())));
        inventoryLocationRepository.save(location);
    }

    private InventoryLocation resolveInventoryLocation(Long companyId, Long productId, Long locationId) {
        if (locationId != null) {
            InventoryLocation location = inventoryLocationRepository.findById(locationId)
                    .orElseThrow(() -> new IllegalStateException("Inventory location not found: " + locationId));
            if (location.getCompanyId() != null && !location.getCompanyId().equals(companyId)) {
                throw new IllegalStateException("Inventory location does not belong to the current company.");
            }
            return location;
        }

        List<InventoryLocation> productLocations = inventoryLocationRepository.findByCompanyIdAndProductId(companyId, productId);
        if (productLocations.isEmpty()) {
            throw new IllegalStateException("No inventory location found for product ID: " + productId);
        }
        return productLocations.get(0);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
