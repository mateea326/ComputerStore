package com.example.ComputerStore.repo;

import com.example.ComputerStore.model.Wishlist;
import com.example.ComputerStore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    Optional<Wishlist> findByUser(User user);
}
