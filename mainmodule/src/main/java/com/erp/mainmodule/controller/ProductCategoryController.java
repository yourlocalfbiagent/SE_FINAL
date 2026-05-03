package com.erp.mainmodule.controller;

import com.erp.mainmodule.entity.ProductCategory;
import com.erp.mainmodule.service.ProductCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class ProductCategoryController {

    private final ProductCategoryService service;

    public ProductCategoryController(ProductCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductCategory> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ProductCategory create(@RequestBody ProductCategory category) {
        return service.create(category);
    }

    @GetMapping("/{id}")
    public ProductCategory getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public ProductCategory update(@PathVariable Integer id, @RequestBody ProductCategory category) {
        return service.update(id, category);
    }
}