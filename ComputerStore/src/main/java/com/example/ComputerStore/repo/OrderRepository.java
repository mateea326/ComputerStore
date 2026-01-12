package com.example.ComputerStore.repo;

import com.example.ComputerStore.model.Customer;
import com.example.ComputerStore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByCustomer(Customer customer);

}