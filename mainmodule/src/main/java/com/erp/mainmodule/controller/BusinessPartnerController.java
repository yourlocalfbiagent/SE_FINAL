package com.erp.mainmodule.controller;

import com.erp.mainmodule.entity.BusinessPartner;
import com.erp.mainmodule.service.BusinessPartnerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/partners")
public class BusinessPartnerController {

    private final BusinessPartnerService service;

    public BusinessPartnerController(BusinessPartnerService service) {
        this.service = service;
    }

    @GetMapping
    public List<BusinessPartner> getAll() {
        return service.getAll();
    }

    @PostMapping
    public BusinessPartner create(@RequestBody BusinessPartner partner) {
        return service.create(partner);
    }

    @GetMapping("/{id}")
    public BusinessPartner getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public BusinessPartner update(@PathVariable Integer id, @RequestBody BusinessPartner partner) {
        return service.update(id, partner);
    }
}