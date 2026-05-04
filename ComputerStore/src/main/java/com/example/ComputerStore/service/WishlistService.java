package com.example.ComputerStore.service;

import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.model.Wishlist;
import com.example.ComputerStore.repo.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserService userService;
    private final ProductService productService;

    public WishlistService(WishlistRepository wishlistRepository,
                           UserService userService,
                           ProductService productService) {
        this.wishlistRepository = wishlistRepository;
        this.userService = userService;
        this.productService = productService;
    }

    public Wishlist getOrCreateWishlist(Integer userId) {
        User user = userService.findUserById(userId);
        return wishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist(user);
                    return wishlistRepository.save(wishlist);
                });
    }

    public Wishlist addProductToWishlist(Integer userId, Integer productId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        Product product = productService.getProductDetails(productId);

        if (wishlist.containsProduct(product)) {
            throw new com.example.ComputerStore.exception.DuplicateResourceException("Product", "wishlist", productId);
        }

        wishlist.addProduct(product);
        return wishlistRepository.save(wishlist);
    }

    public Wishlist removeProductFromWishlist(Integer userId, Integer productId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        Product product = productService.getProductDetails(productId);
        wishlist.removeProduct(product);
        return wishlistRepository.save(wishlist);
    }

    public Set<Product> getWishlistProducts(Integer userId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        return wishlist.getProducts();
    }

    // verifica daca un produs este in wishlist
    public boolean isProductInWishlist(Integer userId, Integer productId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        Product product = productService.getProductDetails(productId);
        return wishlist.containsProduct(product);
    }

    public Set<Integer> getWishlistProductIds(Integer userId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        return wishlist.getProducts().stream()
                .map(Product::getProductId)
                .collect(java.util.stream.Collectors.toSet());
    }
}


