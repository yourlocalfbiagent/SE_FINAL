package com.app.se_final_sales.repository;

import com.sefinal.erp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCompanyId(Long companyId);
    List<Product> findByIsActiveTrue();
    Optional<Product> findBySku(String sku);
}
