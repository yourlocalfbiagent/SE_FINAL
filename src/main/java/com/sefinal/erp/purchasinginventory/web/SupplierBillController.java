// SupplierBillController.java
package com.sefinal.erp.purchasinginventory.web;

import com.sefinal.erp.purchasinginventory.dao.SupplierBillDao;
import com.sefinal.erp.purchasinginventory.model.SupplierBill;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/supplier-bills")
@Tag(name = "Supplier Bills", description = "Manage supplier bills")
public class SupplierBillController {

    private final SupplierBillDao supplierBillDao;

    public SupplierBillController(SupplierBillDao supplierBillDao) {
        this.supplierBillDao = supplierBillDao;
    }

    @GetMapping
    @Operation(summary = "List all supplier bills")
    public List<SupplierBill> getAllSupplierBills() {
        return supplierBillDao.findAll();
    }

    @PostMapping
    @Operation(summary = "Create a new supplier bill")
    public SupplierBill createSupplierBill(@RequestBody SupplierBill supplierBill) {
        return supplierBillDao.save(supplierBill);
    }
}