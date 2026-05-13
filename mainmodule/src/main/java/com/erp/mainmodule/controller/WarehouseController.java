package com.erp.mainmodule.controller;

import com.erp.mainmodule.entity.Warehouse;
import com.erp.mainmodule.service.WarehouseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
public class WarehouseController {

    private final WarehouseService service;

    public WarehouseController(WarehouseService service) {
        this.service = service;
    }

    @GetMapping
    public List<Warehouse> getAll() {
        return service.getAll();
    }

    @PostMapping
    public Warehouse create(@RequestBody Warehouse warehouse) {
        return service.create(warehouse);
    }

    @GetMapping("/{id}")
    public Warehouse getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public Warehouse update(@PathVariable Integer id, @RequestBody Warehouse warehouse) {
        return service.update(id, warehouse);
    }
}