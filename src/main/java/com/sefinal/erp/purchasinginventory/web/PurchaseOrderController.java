// PurchaseOrderController.java
package com.sefinal.erp.purchasinginventory.web;

import com.sefinal.erp.purchasinginventory.dao.PurchaseOrderDao;
import com.sefinal.erp.purchasinginventory.model.PurchaseOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@Tag(name = "Purchase Orders", description = "Create and manage purchase orders")
public class PurchaseOrderController {

    private final PurchaseOrderDao purchaseOrderDao;

    public PurchaseOrderController(PurchaseOrderDao purchaseOrderDao) {
        this.purchaseOrderDao = purchaseOrderDao;
    }

    @GetMapping
    @Operation(summary = "List all purchase orders")
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderDao.findAll();
    }

    @PostMapping
    @Operation(summary = "Create a new purchase order")
    public PurchaseOrder createPurchaseOrder(@RequestBody PurchaseOrder purchaseOrder) {
        return purchaseOrderDao.save(purchaseOrder);
    }
}