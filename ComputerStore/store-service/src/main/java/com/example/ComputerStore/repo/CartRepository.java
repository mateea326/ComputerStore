package com.example.ComputerStore.repo;

import com.example.ComputerStore.model.Cart;
import com.example.ComputerStore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUser(User user);
}
