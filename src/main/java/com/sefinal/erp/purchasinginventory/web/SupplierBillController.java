package com.sefinal.erp.purchasinginventory.web;

import com.sefinal.erp.purchasinginventory.dao.SupplierBillDao;
import com.sefinal.erp.purchasinginventory.model.SupplierBill;
import com.sefinal.erp.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier-bills")
@Tag(name = "Supplier Bills", description = "Manage supplier bills")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.read')")
public class SupplierBillController {

    private final SupplierBillDao supplierBillDao;

    public SupplierBillController(SupplierBillDao supplierBillDao) {
        this.supplierBillDao = supplierBillDao;
    }

    @GetMapping
    @Operation(summary = "List all supplier bills")
    public List<SupplierBill> getAllSupplierBills() {
        return supplierBillDao.findByCompanyId(SecurityUtils.getCompanyId());
    }

    @PostMapping
    @Operation(summary = "Create a new supplier bill")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.create')")
    public SupplierBill createSupplierBill(@RequestBody SupplierBill supplierBill) {
        supplierBill.setCompanyId(SecurityUtils.getCompanyId());
        if (supplierBill.getSupplierId() == null) supplierBill.setSupplierId(supplierBill.getPartnerId());
        return supplierBillDao.save(supplierBill);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a supplier bill")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.update')")
    public ResponseEntity<SupplierBill> updateSupplierBill(@PathVariable Long id, @RequestBody SupplierBill supplierBill) {
        if (!supplierBillDao.existsById(id)) return ResponseEntity.notFound().build();
        supplierBill.setBillId(id);
        supplierBill.setCompanyId(SecurityUtils.getCompanyId());
        if (supplierBill.getSupplierId() == null) supplierBill.setSupplierId(supplierBill.getPartnerId());
        return ResponseEntity.ok(supplierBillDao.save(supplierBill));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a supplier bill")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.delete')")
    public ResponseEntity<Void> deleteSupplierBill(@PathVariable Long id) {
        if (!supplierBillDao.existsById(id)) return ResponseEntity.notFound().build();
        supplierBillDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
