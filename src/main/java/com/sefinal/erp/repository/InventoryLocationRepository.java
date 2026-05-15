package com.sefinal.erp.repository;

import com.sefinal.erp.entity.InventoryLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLocationRepository extends JpaRepository<InventoryLocation, Long> {
    List<InventoryLocation> findByWarehouseId(Long warehouseId);
    List<InventoryLocation> findByProductId(Long productId);
    List<InventoryLocation> findByCompanyId(Long companyId);
    List<InventoryLocation> findByCompanyIdAndProductId(Long companyId, Long productId);
}
