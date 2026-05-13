package com.erp.mainmodule.service;

import com.erp.mainmodule.entity.Product;
import com.erp.mainmodule.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    // GET ALL
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    // CREATE
    public Product createProduct(Product product) {
        return repository.save(product);
    }

    // GET BY ID
    public Product getProductById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    // DELETE
    public void deleteProduct(Integer id) {
        repository.deleteById(id);
    }


    public Product updateProduct(Integer id, Product updatedProduct) {
        Product existing = repository.findById(id).orElse(null);

        if (existing == null) {
            throw new RuntimeException("User not found");
        }

        existing.setProductName(updatedProduct.getProductName());
        existing.setSku(updatedProduct.getSku());
        existing.setCostPrice(updatedProduct.getCostPrice());
        existing.setSellingPrice(updatedProduct.getSellingPrice());
        existing.setCompanyId(updatedProduct.getCompanyId());

        return repository.save(existing);
    }
}