package com.example.ComputerStore.service;

import com.example.ComputerStore.exception.ResourceNotFoundException;
import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.repo.ProductRepository;
import com.example.ComputerStore.repo.MotherboardRepository;
import com.example.ComputerStore.repo.GraphicsCardRepository;
import com.example.ComputerStore.repo.ProcessorRepository;
import com.example.ComputerStore.repo.CaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final MotherboardRepository motherboardRepository;
    private final GraphicsCardRepository graphicsCardRepository;
    private final ProcessorRepository processorRepository;
    private final CaseRepository caseRepository;

    public ProductService(ProductRepository productRepository,
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

    // CREATE / UPDATE
    public Product saveProduct(Product product) {
        Product saved = productRepository.save(product);
        log.info("Product saved: id={}, name={}", saved.getProductId(), saved.getName());
        return saved;
    }

    // READ – toate produsele (fără paginare)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // READ – toate produsele cu paginare
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    // READ – produs după ID
    public Product getProductDetails(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    // UPDATE – editare produs existent
    public Product updateProduct(Integer id, Product updatedProduct) {
        Product existingProduct = getProductDetails(id);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());

        Product saved = productRepository.save(existingProduct);
        log.info("Product updated: id={}", id);
        return saved;
    }

    // DELETE
    public void deleteProduct(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted: id={}", id);
    }

    // READ – filtrare după tip de produs
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