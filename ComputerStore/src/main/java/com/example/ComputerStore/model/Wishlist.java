package com.example.ComputerStore.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "wishlists")
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer wishlistId;

    // relatie 1:1 cu User (fiecare user are un singur wishlist)
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // relatie Many-to-Many cu Product
    @ManyToMany
    @JoinTable(
        name = "wishlist_products",
        joinColumns = @JoinColumn(name = "wishlist_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();

    public Wishlist() {}

    public Wishlist(User user) {
        this.user = user;
    }

    // Getters and Setters
    public Integer getWishlistId() { return wishlistId; }
    public void setWishlistId(Integer wishlistId) { this.wishlistId = wishlistId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Set<Product> getProducts() { return products; }
    public void setProducts(Set<Product> products) { this.products = products; }

    public void addProduct(Product product) {
        products.add(product);
    }

    public void removeProduct(Product product) {
        products.remove(product);
    }

    public boolean containsProduct(Product product) {
        return products.contains(product);
    }
}
