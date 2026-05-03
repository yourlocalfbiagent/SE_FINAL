package com.erp.mainmodule.service;

import com.erp.mainmodule.entity.BusinessPartner;
import com.erp.mainmodule.repository.BusinessPartnerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessPartnerService {

    private final BusinessPartnerRepository repository;

    public BusinessPartnerService(BusinessPartnerRepository repository) {
        this.repository = repository;
    }

    // GET ALL
    public List<BusinessPartner> getAll() {
        return repository.findAll();
    }

    // CREATE
    public BusinessPartner create(BusinessPartner partner) {
        return repository.save(partner);
    }

    // GET BY ID
    public BusinessPartner getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    // DELETE
    public void delete(Integer id) {
        repository.deleteById(id);
    }


    public BusinessPartner update(Integer id, BusinessPartner updated) {
        BusinessPartner existing = repository.findById(id).orElse(null);

        if (existing == null) {
            throw new RuntimeException("User not found");
        }

        existing.setPartnerName(updated.getPartnerName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        existing.setCity(updated.getCity());
        existing.setCountry(updated.getCountry());
        existing.setType(updated.getType());
        existing.setCompany(updated.getCompany());

        return repository.save(existing);
    }
}