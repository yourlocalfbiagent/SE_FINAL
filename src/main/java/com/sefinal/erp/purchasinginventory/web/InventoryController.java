// InventoryController.java
package com.sefinal.erp.purchasinginventory.web;

import com.sefinal.erp.purchasinginventory.dao.InventoryCountDao;
import com.sefinal.erp.purchasinginventory.dao.StockMovementDao;
import com.sefinal.erp.purchasinginventory.model.InventoryCount;
import com.sefinal.erp.purchasinginventory.model.StockMovement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Inventory counts and stock movements")
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
        return inventoryCountDao.findAll();
    }

    @GetMapping("/stock-movements")
    @Operation(summary = "List all stock movements")
    public List<StockMovement> getAllStockMovements() {
        return stockMovementDao.findAll();
    }

    @PostMapping("/counts")
    @Operation(summary = "Create a new inventory count")
    public InventoryCount createInventoryCount(@RequestBody InventoryCount inventoryCount) {
        return inventoryCountDao.save(inventoryCount);
    }

    @PostMapping("/stock-movements")
    @Operation(summary = "Record a new stock movement")
    public StockMovement createStockMovement(@RequestBody StockMovement stockMovement) {
        return stockMovementDao.save(stockMovement);
    }
}