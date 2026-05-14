package com.sefinal.erp.controller;

import com.sefinal.erp.entity.Warehouse;
import com.sefinal.erp.exception.ResourceNotFoundException;
import com.sefinal.erp.repository.WarehouseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouses", description = "Manage warehouse locations")
public class WarehouseController {

    private final WarehouseRepository repo;

    @GetMapping
    @Operation(summary = "List all warehouses")
    public List<Warehouse> getAll() {
        return repo.findAll();
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "List warehouses by company")
    public List<Warehouse> getByCompany(@PathVariable Long companyId) {
        return repo.findByCompanyId(companyId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID")
    public Warehouse getById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + id));
    }

    @PostMapping
    @Operation(summary = "Create a warehouse")
    public ResponseEntity<Warehouse> create(@Valid @RequestBody Warehouse warehouse) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(warehouse));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a warehouse")
    public Warehouse update(@PathVariable Long id, @Valid @RequestBody Warehouse warehouse) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Warehouse not found: " + id);
        warehouse.setWarehouseId(id);
        return repo.save(warehouse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a warehouse")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Warehouse not found: " + id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
