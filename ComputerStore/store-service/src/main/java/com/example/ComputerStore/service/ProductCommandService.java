package com.example.ComputerStore.service;

import com.example.ComputerStore.model.*;
import com.example.ComputerStore.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCommandService {

    private static final Logger log = LoggerFactory.getLogger(ProductCommandService.class);

    private final ProductRepository productRepository;
    private final ProductQueryService productQueryService;
    private final WishlistRepository wishlistRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    public ProductCommandService(ProductRepository productRepository,
                                 ProductQueryService productQueryService,
                                 WishlistRepository wishlistRepository,
                                 OrderItemRepository orderItemRepository,
                                 CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.productQueryService = productQueryService;
        this.wishlistRepository = wishlistRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // de fiecare data cand adaugam un produs nou cache-urile de citire devin vechi
    // asa ca stergem datele din cache-uri facand ca urmatoarele apeluri sa faca interogari SQL si sa refaca cache-ul
    @CacheEvict(value = {"products_all", "products_by_type", "product"}, allEntries = true)
    public Product saveProduct(Product product) {
        Product saved = productRepository.save(product);
        log.info("Product saved via CommandService: id={}, name={}", saved.getProductId(), saved.getName());
        return saved;
    }

    @Transactional
    @CacheEvict(value = {"products_all", "products_by_type", "product"}, allEntries = true)
    public Product updateProduct(Integer id, Product updated) {
        Product existing = productQueryService.getProductDetails(id);
        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        if (updated.getImageUrl() != null && !updated.getImageUrl().isBlank()) {
            existing.setImageUrl(updated.getImageUrl());
        }

        // converteste automat produsul la subclasa sa
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
        log.info("Product updated via CommandService: id={}", id);
        return saved;
    }

    // inainte de a sterge un produs trebuie sa stergem toate referintele sale la celelalte tabele

    @Transactional
    @CacheEvict(value = {"products_all", "products_by_type", "product"}, allEntries = true)
    public void deleteProduct(Integer id) {
        Product product = productQueryService.getProductDetails(id);
        
        wishlistRepository.removeProductFromAllWishlists(id);
        orderItemRepository.deleteByProduct(product);
        cartItemRepository.deleteByProduct(product);
        
        productRepository.delete(product);
        log.info("Product and related items deleted via CommandService: id={}", id);
    }
}
