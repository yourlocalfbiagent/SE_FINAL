// StockMovementDao.java
package com.sefinal.erp.purchasinginventory.dao;

import com.sefinal.erp.purchasinginventory.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockMovementDao extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByCompanyId(Long companyId);
}