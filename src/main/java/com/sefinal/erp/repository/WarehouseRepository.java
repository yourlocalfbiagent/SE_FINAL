package com.sefinal.erp.repository;

import com.sefinal.erp.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByCompanyId(Long companyId);
    List<Warehouse> findByIsActiveTrue();
}
