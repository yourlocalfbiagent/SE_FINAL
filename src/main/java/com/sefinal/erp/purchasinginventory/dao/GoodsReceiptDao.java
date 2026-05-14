// GoodsReceiptDao.java
package com.sefinal.erp.purchasinginventory.dao;

import com.sefinal.erp.purchasinginventory.model.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoodsReceiptDao extends JpaRepository<GoodsReceipt, Long> {
    List<GoodsReceipt> findByCompanyId(Long companyId);
}