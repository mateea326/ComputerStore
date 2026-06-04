package com.example.ComputerStore.repo;

import com.example.ComputerStore.model.User;
import com.example.ComputerStore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUser(User user);
    Page<Order> findByUser(User user, Pageable pageable);
}