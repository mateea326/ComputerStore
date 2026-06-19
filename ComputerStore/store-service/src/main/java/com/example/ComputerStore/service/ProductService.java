package com.example.ComputerStore.service;

import com.example.ComputerStore.exception.ResourceNotFoundException;
import com.example.ComputerStore.model.*;
import com.example.ComputerStore.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductQueryService productQueryService;
    private final ProductCommandService productCommandService;

    public ProductService(ProductQueryService productQueryService,
                          ProductCommandService productCommandService) {
        this.productQueryService = productQueryService;
        this.productCommandService = productCommandService;
    }

    // CREATE / UPDATE
    public Product saveProduct(Product product) {
        return productCommandService.saveProduct(product);
    }

    // READ
    public List<Product> getAllProducts() {
        return productQueryService.getAllProducts();
    }

    // READ
    public Page<Product> getAllProducts(Pageable pageable) {
        return productQueryService.getAllProducts(pageable);
    }

    // READ
    public Product getProductDetails(Integer id) {
        return productQueryService.getProductDetails(id);
    }

    // READ
    public Page<Product> getAllProductsOrderByPopularity(Pageable pageable) {
        return productQueryService.getAllProductsOrderByPopularity(pageable);
    }

    // READ
    public Page<Product> searchProducts(String search, Pageable pageable) {
        return productQueryService.searchProducts(search, pageable);
    }

    // UPDATE
    public Product updateProduct(Integer id, Product updated) {
        return productCommandService.updateProduct(id, updated);
    }

    // DELETE
    public void deleteProduct(Integer id) {
        productCommandService.deleteProduct(id);
    }

    // READ
    public List<? extends Product> filterProductsByType(String type) {
        return productQueryService.filterProductsByType(type);
    }
}