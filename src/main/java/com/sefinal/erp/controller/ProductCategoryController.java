package com.sefinal.erp.controller;

import com.sefinal.erp.entity.ProductCategory;
import com.sefinal.erp.exception.ResourceNotFoundException;
import com.sefinal.erp.repository.ProductCategoryRepository;
import com.sefinal.erp.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
@Tag(name = "Product Categories", description = "Manage product category hierarchy")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.read') or hasAuthority('SALES.read') or hasAuthority('PURCHASING.read')")
public class ProductCategoryController {

    private final ProductCategoryRepository repo;

    @GetMapping
    @Operation(summary = "List all product categories")
    public List<ProductCategory> getAll() {
        return repo.findByCompanyId(SecurityUtils.getCompanyId());
    }

    @GetMapping("/active")
    @Operation(summary = "List active product categories")
    public List<ProductCategory> getActive() {
        return repo.findByIsActiveTrue();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product category by ID")
    public ProductCategory getById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory not found: " + id));
    }

    @GetMapping("/{id}/subcategories")
    @Operation(summary = "List subcategories of a category")
    public List<ProductCategory> getSubcategories(@PathVariable Long id) {
        return repo.findByParentCategoryId(id);
    }

    @PostMapping
    @Operation(summary = "Create a product category")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.create')")
    public ResponseEntity<ProductCategory> create(@Valid @RequestBody ProductCategory category) {
        category.setCompanyId(SecurityUtils.getCompanyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product category")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.update')")
    public ProductCategory update(@PathVariable Long id, @Valid @RequestBody ProductCategory category) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("ProductCategory not found: " + id);
        category.setCategoryId(id);
        return repo.save(category);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product category")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("ProductCategory not found: " + id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
