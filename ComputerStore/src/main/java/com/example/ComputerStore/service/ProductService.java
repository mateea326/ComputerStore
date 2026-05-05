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

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final MotherboardRepository motherboardRepository;
    private final GraphicsCardRepository graphicsCardRepository;
    private final ProcessorRepository processorRepository;
    private final CaseRepository caseRepository;
    private final WishlistRepository wishlistRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    public ProductService(ProductRepository productRepository,
                          MotherboardRepository motherboardRepository,
                          GraphicsCardRepository graphicsCardRepository,
                          ProcessorRepository processorRepository,
                          CaseRepository caseRepository,
                          WishlistRepository wishlistRepository,
                          OrderItemRepository orderItemRepository,
                          CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.motherboardRepository = motherboardRepository;
        this.graphicsCardRepository = graphicsCardRepository;
        this.processorRepository = processorRepository;
        this.caseRepository = caseRepository;
        this.wishlistRepository = wishlistRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
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

    // READ - Popularity
    public Page<Product> getAllProductsOrderByPopularity(Pageable pageable) {
        return productRepository.findAllOrderByPopularity(pageable);
    }

    // UPDATE – editare produs existent
    public Product updateProduct(Integer id, Product updated) {
        Product existing = getProductDetails(id);
        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());

        if (existing instanceof Processor p && updated instanceof Processor u) {
            p.setCoreCount(u.getCoreCount());
            p.setCoreClock(u.getCoreClock());
            p.setSocket(u.getSocket());
        } else if (existing instanceof GraphicsCard g && updated instanceof GraphicsCard u) {
            g.setMemorySize(u.getMemorySize());
            g.setCoreClock(u.getCoreClock());
            g.setMemoryClock(u.getMemoryClock());
        } else if (existing instanceof Motherboard m && updated instanceof Motherboard u) {
            m.setSlots(u.getSlots());
            m.setCpu_socket(u.getCpu_socket());
            m.setChipset(u.getChipset());
        } else if (existing instanceof Case c && updated instanceof Case u) {
            c.setVents(u.getVents());
            c.setType(u.getType());
            c.setFormat(u.getFormat());
        }

        Product saved = productRepository.save(existing);
        log.info("Product updated: id={}", id);
        return saved;
    }

    // DELETE
    @org.springframework.transaction.annotation.Transactional
    public void deleteProduct(Integer id) {
        Product product = getProductDetails(id);
        
        // Remove from wishlists (native query for join table)
        wishlistRepository.removeProductFromAllWishlists(id);
        
        // Remove from order items
        orderItemRepository.deleteByProduct(product);
        
        // Remove from cart items
        cartItemRepository.deleteByProduct(product);
        
        productRepository.delete(product);
        log.info("Product and its related items deleted: id={}", id);
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