package com.sefinal.erp.controller;

import com.sefinal.erp.entity.BulkImport;
import com.sefinal.erp.entity.BulkImportError;
import com.sefinal.erp.exception.ResourceNotFoundException;
import com.sefinal.erp.repository.BulkImportErrorRepository;
import com.sefinal.erp.repository.BulkImportRepository;
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
@RequestMapping("/api/bulk-imports")
@RequiredArgsConstructor
@Tag(name = "Bulk Imports", description = "Track bulk data import jobs and their errors")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.read')")
public class BulkImportController {

    private final BulkImportRepository importRepo;
    private final BulkImportErrorRepository errorRepo;

    @GetMapping
    @Operation(summary = "List all bulk imports")
    public List<BulkImport> getAll() {
        return importRepo.findAll();
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "List bulk imports by company")
    public List<BulkImport> getByCompany(@PathVariable Long companyId) {
        return importRepo.findByCompanyId(companyId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bulk import by ID")
    public BulkImport getById(@PathVariable Long id) {
        return importRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BulkImport not found: " + id));
    }

    @GetMapping("/{id}/errors")
    @Operation(summary = "List errors for a bulk import")
    public List<BulkImportError> getErrors(@PathVariable Long id) {
        if (!importRepo.existsById(id)) throw new ResourceNotFoundException("BulkImport not found: " + id);
        return errorRepo.findByImportId(id);
    }

    @PostMapping
    @Operation(summary = "Create a bulk import record")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.create')")
    public ResponseEntity<BulkImport> create(@Valid @RequestBody BulkImport bulkImport) {
        return ResponseEntity.status(HttpStatus.CREATED).body(importRepo.save(bulkImport));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a bulk import record")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.update')")
    public BulkImport update(@PathVariable Long id, @Valid @RequestBody BulkImport bulkImport) {
        if (!importRepo.existsById(id)) throw new ResourceNotFoundException("BulkImport not found: " + id);
        bulkImport.setImportId(id);
        return importRepo.save(bulkImport);
    }
}
