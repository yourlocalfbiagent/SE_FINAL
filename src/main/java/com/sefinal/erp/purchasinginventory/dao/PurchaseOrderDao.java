package com.sefinal.erp.purchasinginventory.dao;

import com.sefinal.erp.purchasinginventory.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseOrderDao extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByCompanyId(Long companyId);
}
