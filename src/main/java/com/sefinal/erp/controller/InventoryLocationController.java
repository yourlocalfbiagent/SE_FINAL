package com.sefinal.erp.controller;

import com.sefinal.erp.entity.InventoryLocation;
import com.sefinal.erp.exception.ResourceNotFoundException;
import com.sefinal.erp.repository.InventoryLocationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-locations")
@RequiredArgsConstructor
@Tag(name = "Inventory Locations", description = "Manage stock positions within warehouses")
public class InventoryLocationController {

    private final InventoryLocationRepository repo;

    @GetMapping
    @Operation(summary = "List all inventory locations")
    public List<InventoryLocation> getAll() {
        return repo.findAll();
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "List inventory locations by warehouse")
    public List<InventoryLocation> getByWarehouse(@PathVariable Long warehouseId) {
        return repo.findByWarehouseId(warehouseId);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "List inventory locations by product")
    public List<InventoryLocation> getByProduct(@PathVariable Long productId) {
        return repo.findByProductId(productId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory location by ID")
    public InventoryLocation getById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryLocation not found: " + id));
    }

    @PostMapping
    @Operation(summary = "Create an inventory location")
    public ResponseEntity<InventoryLocation> create(@Valid @RequestBody InventoryLocation location) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(location));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an inventory location")
    public InventoryLocation update(@PathVariable Long id, @Valid @RequestBody InventoryLocation location) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("InventoryLocation not found: " + id);
        location.setLocationId(id);
        return repo.save(location);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an inventory location")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("InventoryLocation not found: " + id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
