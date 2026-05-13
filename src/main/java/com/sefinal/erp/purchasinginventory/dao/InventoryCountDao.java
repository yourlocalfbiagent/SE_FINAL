// InventoryCountDao.java
package com.sefinal.erp.purchasinginventory.dao;

import com.sefinal.erp.purchasinginventory.model.InventoryCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryCountDao extends JpaRepository<InventoryCount, Integer> {
}