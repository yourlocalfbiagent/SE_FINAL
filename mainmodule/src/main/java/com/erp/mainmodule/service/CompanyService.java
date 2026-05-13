package com.erp.mainmodule.service;

import com.erp.mainmodule.entity.Company;
import com.erp.mainmodule.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository repository;

    public CompanyService(CompanyRepository repository) {
        this.repository = repository;
    }

    public List<Company> getAll() {
        return repository.findAll();
    }

    public Company create(Company company) {
        return repository.save(company);
    }

    public Company getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Company update(Integer id, Company updated) {
        Company existing = getById(id);

        existing.setCompanyName(updated.getCompanyName());
        existing.setCurrency(updated.getCurrency());
        existing.setLocale(updated.getLocale());

        return repository.save(existing);
    }
}