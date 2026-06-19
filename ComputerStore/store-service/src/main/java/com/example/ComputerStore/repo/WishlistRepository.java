package com.example.ComputerStore.repo;

import com.example.ComputerStore.model.Wishlist;
import com.example.ComputerStore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    Optional<Wishlist> findByUser(User user);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM wishlist_products WHERE product_id = :productId", nativeQuery = true)
    void removeProductFromAllWishlists(Integer productId);

    // facem o interogare nativa deoarece avem un tabela asociativa pentru wishlist si produse, iar aceasta tabela nu este mapata ca o entitate in JPA
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM wishlist_products WHERE wishlist_id = :wishlistId", nativeQuery = true)
    void deleteProductsByWishlistId(int wishlistId);
}
