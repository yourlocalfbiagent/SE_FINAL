package com.sefinal.erp.admin.web;

import com.sefinal.erp.admin.auth.AuthInterceptor;
import com.sefinal.erp.admin.auth.CurrentUser;
import com.sefinal.erp.admin.model.AuditLog;
import com.sefinal.erp.admin.model.Company;
import com.sefinal.erp.admin.repository.AuditLogRepository;
import com.sefinal.erp.admin.repository.CompanyRepository;
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

    private final CompanyRepository companies;
    private final AuditLogRepository auditRepo;

    public CompanyController(CompanyRepository companies, AuditLogRepository auditRepo) {
        this.companies = companies;
        this.auditRepo = auditRepo;
    }

    @GetMapping
    public List<Company> list() { return companies.findAll(); }

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
        Company c = new Company();
        c.setCompanyName(req.companyName());
        c.setCurrency(req.currency() != null ? req.currency() : "USD");
        c.setTaxDefault(req.taxDefault() != null ? req.taxDefault() : BigDecimal.ZERO);
        c.setLocale(req.locale() != null ? req.locale() : "en-US");
        c.setActive(req.isActive() == null || req.isActive());
        Company created = companies.save(c);

        CurrentUser cu = AuthInterceptor.current(request);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "company.create", "company",
                created.getCompanyId(), null));
        return ResponseEntity.created(URI.create("/api/companies/" + created.getCompanyId())).body(created);
    }

    @PutMapping("/{id}")
    public Company update(@PathVariable int id, @RequestBody CreateCompanyRequest req, HttpServletRequest request) {
        Company c = companies.findById(id)
                .orElseThrow(() -> new NotFoundException("company " + id + " not found"));
        if (req.companyName() == null || req.companyName().isBlank()) {
            throw new BadRequestException("companyName is required");
        }
        c.setCompanyName(req.companyName());
        c.setCurrency(req.currency() != null ? req.currency() : "USD");
        c.setTaxDefault(req.taxDefault() != null ? req.taxDefault() : BigDecimal.ZERO);
        c.setLocale(req.locale() != null ? req.locale() : "en-US");
        c.setActive(req.isActive() == null || req.isActive());
        Company updated = companies.save(c);

        CurrentUser cu = AuthInterceptor.current(request);
        auditRepo.save(new AuditLog(cu.userId(), cu.companyId(), "company.update", "company", id, null));
        return updated;
    }
}
