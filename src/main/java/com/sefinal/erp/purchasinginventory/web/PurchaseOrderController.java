// PurchaseOrderController.java
package com.sefinal.erp.purchasinginventory.web;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sefinal.erp.purchasinginventory.dao.PurchaseOrderDao;
import com.sefinal.erp.purchasinginventory.model.PurchaseOrder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    @GetMapping("/{id}")
public PurchaseOrder getPurchaseOrderById(@PathVariable Integer id) {
 return purchaseOrderDao.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
}
@ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Create a new purchase order")
    public PurchaseOrder createPurchaseOrder(@RequestBody PurchaseOrder purchaseOrder) {
        return purchaseOrderDao.save(purchaseOrder);
    }
   @PutMapping("/{id}")
public PurchaseOrder updatePurchaseOrder(@PathVariable Integer id, @RequestBody PurchaseOrder purchaseOrder) {
    if (!purchaseOrderDao.existsById(id)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found");
    }

    purchaseOrder.setPoId(Long.valueOf(id));
    return purchaseOrderDao.save(purchaseOrder);
}
   @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a purchase order by ID")
    public void deletePurchaseOrder(@PathVariable Integer id) {
        purchaseOrderDao.deleteById(id);
    }
}