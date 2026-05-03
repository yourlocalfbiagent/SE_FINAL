package com.erp.mainmodule.service;

import com.erp.mainmodule.entity.ProductCategory;
import com.erp.mainmodule.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository repository;

    public ProductCategoryService(ProductCategoryRepository repository) {
        this.repository = repository;
    }

    public List<ProductCategory> getAll() {
        return repository.findAll();
    }

    public ProductCategory create(ProductCategory category) {
        return repository.save(category);
    }

    public ProductCategory getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public ProductCategory update(Integer id, ProductCategory updated) {
        ProductCategory existing = getById(id);

        existing.setCategoryName(updated.getCategoryName());
        existing.setParentCategory(updated.getParentCategory());

        return repository.save(existing);
    }
}