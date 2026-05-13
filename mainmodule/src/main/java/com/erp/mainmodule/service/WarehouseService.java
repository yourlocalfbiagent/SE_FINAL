package com.erp.mainmodule.service;

import com.erp.mainmodule.entity.Warehouse;
import com.erp.mainmodule.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository repository;

    public WarehouseService(WarehouseRepository repository) {
        this.repository = repository;
    }

    public List<Warehouse> getAll() {
        return repository.findAll();
    }

    public Warehouse create(Warehouse warehouse) {
        return repository.save(warehouse);
    }

    public Warehouse getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Warehouse update(Integer id, Warehouse updated) {
        Warehouse existing = getById(id);

        existing.setWarehouseName(updated.getWarehouseName());
        existing.setAddress(updated.getAddress());
        existing.setCompany(updated.getCompany());

        return repository.save(existing);
    }
}