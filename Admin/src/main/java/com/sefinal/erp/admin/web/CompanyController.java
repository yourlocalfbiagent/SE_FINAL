package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.dao.AuditDao;
import com.sefinal.erp.admin.dao.CompanyDao;
import com.sefinal.erp.admin.model.Company;
import com.sefinal.erp.admin.web.dto.Dtos.CreateCompanyRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyDao companies;
    private final AuditDao audit;

    public CompanyController(CompanyDao companies, AuditDao audit) {
        this.companies = companies;
        this.audit = audit;
    }

    @GetMapping
    public List<Company> list() {
        return companies.findAll();
    }

    @GetMapping("/{id}")
    public Company get(@PathVariable int id) {
        return companies.findById(id)
                .orElseThrow(() -> new NotFoundException("company " + id + " not found"));
    }

    @PostMapping
    public ResponseEntity<Company> create(@RequestBody CreateCompanyRequest req, HttpServletRequest request) {
        if (req.companyName() == null || req.companyName().isBlank()) {
            throw new BadRequestException("companyName is required");
        }
        Company created = companies.create(new Company(
                null,
                req.companyName(),
                req.currency() != null ? req.currency() : "USD",
                req.taxDefault() != null ? req.taxDefault() : BigDecimal.ZERO,
                req.locale() != null ? req.locale() : "en-US",
                req.isActive() == null || req.isActive(),
                null
        ));
        CurrentUser cu = AuthInterceptor.current(request);
        audit.log(cu.userId(), cu.companyId(), "company.create", "company", created.companyId(), null);
        return ResponseEntity.created(URI.create("/api/companies/" + created.companyId())).body(created);
    }

    @PutMapping("/{id}")
    public Company update(@PathVariable int id, @RequestBody CreateCompanyRequest req, HttpServletRequest request) {
        companies.findById(id).orElseThrow(() -> new NotFoundException("company " + id + " not found"));
        if (req.companyName() == null || req.companyName().isBlank()) {
            throw new BadRequestException("companyName is required");
        }
        Company updated = companies.update(new Company(
                id,
                req.companyName(),
                req.currency() != null ? req.currency() : "USD",
                req.taxDefault() != null ? req.taxDefault() : BigDecimal.ZERO,
                req.locale() != null ? req.locale() : "en-US",
                req.isActive() == null || req.isActive(),
                null
        ));
        CurrentUser cu = AuthInterceptor.current(request);
        audit.log(cu.userId(), cu.companyId(), "company.update", "company", id, null);
        return updated;
    }
}
