package com.erp.mainmodule.controller;

import com.erp.mainmodule.entity.InventoryLocation;
import com.erp.mainmodule.service.InventoryLocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryLocationController {

    private final InventoryLocationService service;

    public InventoryLocationController(InventoryLocationService service) {
        this.service = service;
    }

    @GetMapping
    public List<InventoryLocation> getAll() {
        return service.getAll();
    }

    @PostMapping
    public InventoryLocation create(@RequestBody InventoryLocation inventory) {
        return service.create(inventory);
    }

    @GetMapping("/{id}")
    public InventoryLocation getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public InventoryLocation update(@PathVariable Integer id, @RequestBody InventoryLocation inventory) {
        return service.update(id, inventory);
    }
}