// SupplierBillDao.java
package com.sefinal.erp.purchasinginventory.dao;

import com.sefinal.erp.purchasinginventory.model.SupplierBill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierBillDao extends JpaRepository<SupplierBill, Long> {
    List<SupplierBill> findByCompanyId(Long companyId);
}