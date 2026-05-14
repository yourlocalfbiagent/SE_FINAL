// InventoryCountDao.java
package com.sefinal.erp.purchasinginventory.dao;

import com.sefinal.erp.purchasinginventory.model.InventoryCount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryCountDao extends JpaRepository<InventoryCount, Long> {
    List<InventoryCount> findByCompanyId(Long companyId);
}