package com.example.ComputerStore.repo;

import com.example.ComputerStore.model.CartItem;
import com.example.ComputerStore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.product = :product")
    void deleteByProduct(Product product);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void deleteByCartId(int cartId);
}
