package com.example.ComputerStore.service;

import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.repo.ProductRepository;
import com.example.ComputerStore.repo.MotherboardRepository;
import com.example.ComputerStore.repo.GraphicsCardRepository;
import com.example.ComputerStore.repo.ProcessorRepository;
import com.example.ComputerStore.repo.CaseRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MotherboardRepository motherboardRepository;
    private final GraphicsCardRepository graphicsCardRepository;
    private final ProcessorRepository processorRepository;
    private final CaseRepository caseRepository;


    // injectarea tuturor dependentelor in Constructor
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

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // comanda products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // comanda details (afiseaza detaliile unui produs)
    public Product getProductDetails(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product with the id " + id + " was not found"));
    }

    // comanda admin edit product
    public Product updateProduct(Integer id, Product updatedProduct) {
        Product existingProduct = getProductDetails(id);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());

        return productRepository.save(existingProduct);
    }

    // comanda admin delete product
    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }

    // comanda de filtrare a produselor dupa tip
    public List<? extends Product> filterProductsByType(String type) {
        String normalizedType = type.toLowerCase().trim();

        return switch (normalizedType) {
            case "motherboards" -> motherboardRepository.findAll();
            case "graphics cards", "gpus" -> graphicsCardRepository.findAll();
            case "processors", "cpus" -> processorRepository.findAll();
            case "cases" -> caseRepository.findAll();
            default -> throw new IllegalArgumentException("Type of product " + type + " is not valid");
        };
    }

}