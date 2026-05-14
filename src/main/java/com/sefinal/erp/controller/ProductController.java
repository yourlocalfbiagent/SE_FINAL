package com.sefinal.erp.controller;

import com.sefinal.erp.entity.Product;
import com.sefinal.erp.exception.ResourceNotFoundException;
import com.app.se_final_sales.repository.ProductRepository;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Manage product catalog")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.read') or hasAuthority('SALES.read') or hasAuthority('PURCHASING.read') or hasAuthority('INVENTORY.read')")
public class ProductController {

    private final ProductRepository repo;

    @GetMapping
    @Operation(summary = "List all products")
    public List<Product> getAll() {
        return repo.findByCompanyId(SecurityUtils.getCompanyId());
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "List products by company")
    public List<Product> getByCompany(@PathVariable Long companyId) {
        return repo.findByCompanyId(companyId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public Product getById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU")
    public Product getBySku(@PathVariable String sku) {
        return repo.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
    }

    @PostMapping
    @Operation(summary = "Create a product")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.create')")
    public ResponseEntity<Product> create(@Valid @RequestBody Product product) {
        product.setCompanyId(SecurityUtils.getCompanyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(product));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.update')")
    public Product update(@PathVariable Long id, @Valid @RequestBody Product product) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Product not found: " + id);
        product.setProductId(id);
        return repo.save(product);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Product not found: " + id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
