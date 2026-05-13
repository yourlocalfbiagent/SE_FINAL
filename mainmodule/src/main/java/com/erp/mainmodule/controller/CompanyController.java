package com.erp.mainmodule.controller;

import com.erp.mainmodule.entity.Company;
import com.erp.mainmodule.service.CompanyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    // GET ALL
    @GetMapping
    public List<Company> getAll() {
        return service.getAll();
    }

    // CREATE
    @PostMapping
    public Company create(@RequestBody Company company) {
        return service.create(company);
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Company getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Company update(@PathVariable Integer id, @RequestBody Company company) {
        return service.update(id, company);
    }
}