// InventoryController.java
package com.sefinal.erp.purchasinginventory.web;

import com.sefinal.erp.purchasinginventory.dao.InventoryCountDao;
import com.sefinal.erp.purchasinginventory.dao.StockMovementDao;
import com.sefinal.erp.purchasinginventory.model.InventoryCount;
import com.sefinal.erp.purchasinginventory.model.StockMovement;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryCountDao inventoryCountDao;
    private final StockMovementDao stockMovementDao;

    public InventoryController(InventoryCountDao inventoryCountDao, StockMovementDao stockMovementDao) {
        this.inventoryCountDao = inventoryCountDao;
        this.stockMovementDao = stockMovementDao;
    }

    @GetMapping("/counts")
    public List<InventoryCount> getAllInventoryCounts() {
        return inventoryCountDao.findAll();
    }

    @GetMapping("/stock-movements")
    public List<StockMovement> getAllStockMovements() {
        return stockMovementDao.findAll();
    }
    @PostMapping("/counts")
public InventoryCount createInventoryCount(@RequestBody InventoryCount inventoryCount) {
    return inventoryCountDao.save(inventoryCount);
}

@PostMapping("/stock-movements")
public StockMovement createStockMovement(@RequestBody StockMovement stockMovement) {
    return stockMovementDao.save(stockMovement);
}
}