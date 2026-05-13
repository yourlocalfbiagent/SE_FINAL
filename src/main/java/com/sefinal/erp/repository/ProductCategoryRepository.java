package com.sefinal.erp.repository;

import com.sefinal.erp.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findByIsActiveTrue();
    List<ProductCategory> findByParentCategoryId(Long parentCategoryId);
}
