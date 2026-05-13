package com.erp.mainmodule.service;

import com.erp.mainmodule.entity.InventoryLocation;
import com.erp.mainmodule.repository.InventoryLocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryLocationService {

    private final InventoryLocationRepository repository;

    public InventoryLocationService(InventoryLocationRepository repository) {
        this.repository = repository;
    }

    public List<InventoryLocation> getAll() {
        return repository.findAll();
    }

    public InventoryLocation create(InventoryLocation inventory) {
        return repository.save(inventory);
    }

    public InventoryLocation getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public InventoryLocation update(Integer id, InventoryLocation updated) {
        InventoryLocation existing = getById(id);

        existing.setLocationName(updated.getLocationName());
        existing.setProduct(updated.getProduct());
        existing.setWarehouse(updated.getWarehouse());
        existing.setQuantityOnHand(updated.getQuantityOnHand());
        existing.setQuantityReserved(updated.getQuantityReserved());
        existing.setQuantityAvailable(updated.getQuantityAvailable());

        return repository.save(existing);
    }
}