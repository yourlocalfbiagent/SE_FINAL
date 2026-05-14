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

import com.sefinal.erp.purchasinginventory.dao.SupplierBillDao;
import com.sefinal.erp.purchasinginventory.model.SupplierBill;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
    @Operation(summary = "Get supplier bill by ID")
    public SupplierBill getSupplierBillById(@PathVariable Integer id) {
        return supplierBillDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Supplier bill not found"
                ));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Create a new supplier bill")
    public SupplierBill createSupplierBill(@Valid @RequestBody SupplierBill supplierBill) {
        return supplierBillDao.save(supplierBill);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier bill by ID")
    public SupplierBill updateSupplierBill(
            @PathVariable Integer id,
            @Valid @RequestBody SupplierBill supplierBill
    ) {
        if (!supplierBillDao.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Supplier bill not found"
            );
        }

        supplierBill.setBillId(Long.valueOf(id));
        return supplierBillDao.save(supplierBill);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier bill by ID")
    public void deleteSupplierBill(@PathVariable Integer id) {
        if (!supplierBillDao.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Supplier bill not found"
            );
        }

        supplierBillDao.deleteById(id);
    }
}