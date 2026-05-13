// PurchaseOrderController.java
package com.sefinal.erp.purchasinginventory.web;

import com.sefinal.erp.purchasinginventory.dao.PurchaseOrderDao;
import com.sefinal.erp.purchasinginventory.model.PurchaseOrder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderDao purchaseOrderDao;

    public PurchaseOrderController(PurchaseOrderDao purchaseOrderDao) {
        this.purchaseOrderDao = purchaseOrderDao;
    }

    @GetMapping
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderDao.findAll();
    }
    @PostMapping
public PurchaseOrder createPurchaseOrder(@RequestBody PurchaseOrder purchaseOrder) {
    return purchaseOrderDao.save(purchaseOrder);
}
}