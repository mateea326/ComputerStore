package com.example.ComputerStore.repo;

import com.example.ComputerStore.model.OrderItem;
import com.example.ComputerStore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    @Modifying
    @Transactional
    @Query("DELETE FROM OrderItem oi WHERE oi.product = :product")
    void deleteByProduct(Product product);

    @Modifying
    @Transactional
    @Query("DELETE FROM OrderItem oi WHERE oi.order.orderId = :orderId")
    void deleteByOrderId(int orderId);
}