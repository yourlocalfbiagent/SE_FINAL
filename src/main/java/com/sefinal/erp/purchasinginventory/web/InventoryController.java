// InventoryController.java
package com.sefinal.erp.purchasinginventory.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sefinal.erp.purchasinginventory.dao.InventoryCountDao;
import com.sefinal.erp.purchasinginventory.dao.StockMovementDao;
import com.sefinal.erp.purchasinginventory.model.InventoryCount;
import com.sefinal.erp.purchasinginventory.model.StockMovement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    @GetMapping("/counts/{id}")
public InventoryCount getInventoryCountById(@PathVariable Integer id) {
    return inventoryCountDao.findById(id).orElse(null);
}

@GetMapping("/stock-movements/{id}")
public StockMovement getStockMovementById(@PathVariable Integer id) {
    return stockMovementDao.findById(id).orElse(null);
}

    @PostMapping("/stock-movements")
    @Operation(summary = "Record a new stock movement")
    public StockMovement createStockMovement(@RequestBody StockMovement stockMovement) {
        return stockMovementDao.save(stockMovement);
    }
}