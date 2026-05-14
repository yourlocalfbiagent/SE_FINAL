package com.sefinal.erp.controller;

import com.sefinal.erp.entity.BusinessPartner;
import com.sefinal.erp.exception.ResourceNotFoundException;
import com.app.se_final_sales.repository.BusinessPartnerRepository;
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
@RequestMapping("/api/business-partners")
@RequiredArgsConstructor
@Tag(name = "Business Partners", description = "Manage customers and suppliers")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.read') or hasAuthority('SALES.read') or hasAuthority('PURCHASING.read')")
public class BusinessPartnerController {

    private final BusinessPartnerRepository repo;

    @GetMapping
    @Operation(summary = "List all business partners")
    public List<BusinessPartner> getAll() {
        return repo.findByCompanyId(SecurityUtils.getCompanyId());
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "List business partners by company")
    public List<BusinessPartner> getByCompany(@PathVariable Long companyId) {
        return repo.findByCompanyId(companyId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get business partner by ID")
    public BusinessPartner getById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessPartner not found: " + id));
    }

    @PostMapping
    @Operation(summary = "Create a business partner")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.create')")
    public ResponseEntity<BusinessPartner> create(@Valid @RequestBody BusinessPartner partner) {
        partner.setCompanyId(SecurityUtils.getCompanyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(partner));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a business partner")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.update')")
    public BusinessPartner update(@PathVariable Long id, @Valid @RequestBody BusinessPartner partner) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("BusinessPartner not found: " + id);
        partner.setPartnerId(id);
        return repo.save(partner);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a business partner")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MASTER DATA.delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("BusinessPartner not found: " + id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
