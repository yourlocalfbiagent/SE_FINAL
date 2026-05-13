// SupplierBillController.java
package com.sefinal.erp.purchasinginventory.web;

import com.sefinal.erp.purchasinginventory.dao.SupplierBillDao;
import com.sefinal.erp.purchasinginventory.model.SupplierBill;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/supplier-bills")
public class SupplierBillController {

    private final SupplierBillDao supplierBillDao;

    public SupplierBillController(SupplierBillDao supplierBillDao) {
        this.supplierBillDao = supplierBillDao;
    }

    @GetMapping
    public List<SupplierBill> getAllSupplierBills() {
        return supplierBillDao.findAll();
    }
    @PostMapping
public SupplierBill createSupplierBill(@RequestBody SupplierBill supplierBill) {
    return supplierBillDao.save(supplierBill);
}
}