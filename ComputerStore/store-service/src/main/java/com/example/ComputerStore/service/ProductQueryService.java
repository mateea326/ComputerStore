package com.example.ComputerStore.service;

import com.example.ComputerStore.exception.ResourceNotFoundException;
import com.example.ComputerStore.model.*;
import com.example.ComputerStore.repo.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final MotherboardRepository motherboardRepository;
    private final GraphicsCardRepository graphicsCardRepository;
    private final ProcessorRepository processorRepository;
    private final CaseRepository caseRepository;

    public ProductQueryService(ProductRepository productRepository,
                               MotherboardRepository motherboardRepository,
                               GraphicsCardRepository graphicsCardRepository,
                               ProcessorRepository processorRepository,
                               CaseRepository caseRepository) {
        this.productRepository = productRepository;
        this.motherboardRepository = motherboardRepository;
        this.graphicsCardRepository = graphicsCardRepository;
        this.processorRepository = processorRepository;
        this.caseRepository = caseRepository;
    }

    @Cacheable(value = "products_all")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> searchProducts(String search, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(search, pageable);
    }

    @Cacheable(value = "product", key = "#id")
    public Product getProductDetails(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    public Page<Product> getAllProductsOrderByPopularity(Pageable pageable) {
        return productRepository.findAllOrderByPopularity(pageable);
    }

    @Cacheable(value = "products_by_type", key = "#type")
    public List<? extends Product> filterProductsByType(String type) {
        String normalizedType = type.toLowerCase().trim();

        return switch (normalizedType) {
            case "motherboards" -> motherboardRepository.findAll();
            case "graphics cards", "gpus" -> graphicsCardRepository.findAll();
            case "processors", "cpus" -> processorRepository.findAll();
            case "cases" -> caseRepository.findAll();
            default -> throw new IllegalArgumentException("Invalid product type: " + type);
        };
    }
}
