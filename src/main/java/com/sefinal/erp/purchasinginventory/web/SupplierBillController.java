// SupplierBillController.java
package com.sefinal.erp.purchasinginventory.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sefinal.erp.purchasinginventory.dao.SupplierBillDao;
import com.sefinal.erp.purchasinginventory.model.SupplierBill;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    @GetMapping("/{id}")
public SupplierBill getSupplierBillById(@PathVariable Integer id) {
    return supplierBillDao.findById(id).orElse(null);
}

    @PostMapping
    @Operation(summary = "Create a new supplier bill")
    public SupplierBill createSupplierBill(@RequestBody SupplierBill supplierBill) {
        return supplierBillDao.save(supplierBill);
    }
}