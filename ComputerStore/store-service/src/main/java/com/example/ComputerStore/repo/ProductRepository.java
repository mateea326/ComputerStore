package com.example.ComputerStore.repo;

import com.example.ComputerStore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query(value = "SELECT p FROM Product p ORDER BY (SELECT COUNT(oi) FROM OrderItem oi WHERE oi.product = p) DESC",
           countQuery = "SELECT COUNT(p) FROM Product p")
    Page<Product> findAllOrderByPopularity(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}