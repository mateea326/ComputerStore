package com.example.ComputerStore.service;

import com.example.ComputerStore.exception.EmptyCartException;
import com.example.ComputerStore.exception.ResourceNotFoundException;
import com.example.ComputerStore.model.*;
import com.example.ComputerStore.repo.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final com.example.ComputerStore.repo.OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository,
                        UserService userService,
                        ProductService productService,
                        com.example.ComputerStore.repo.OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
        this.orderItemRepository = orderItemRepository;
    }

    public List<Order> getOrderHistory(Integer userId) {
        User user = userService.findUserById(userId);
        return orderRepository.findByUser(user);
    }

    public Page<Order> getOrderHistory(Integer userId, Pageable pageable) {
        User user = userService.findUserById(userId);
        return orderRepository.findByUser(user, pageable);
    }

    // Unificarea logicii de creare a comenzii
    public Order createOrder(Integer userId, Map<Integer, Integer> cart) {
        if (cart == null || cart.isEmpty()) {
            throw new EmptyCartException();
        }

        User user = userService.findUserById(userId);

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());

        double totalPrice = 0.0;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Product product = productService.getProductDetails(entry.getKey());
            int quantity = entry.getValue();

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPriceAtPurchase(product.getPrice());

            order.addOrderItem(orderItem);

            totalPrice += product.getPrice() * quantity;
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: id={}, total={}", savedOrder.getOrderId(), savedOrder.getTotalPrice());
        return savedOrder;
    }

    // Alias pentru compatibilitate
    public Order createOrderWithPaymentDetails(Integer userId, Map<Integer, Integer> cart) {
        return createOrder(userId, cart);
    }

    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Order updateOrder(Integer orderId, Map<Integer, Integer> newCart) {
        Order existingOrder = getOrderById(orderId);
        existingOrder.getOrderItems().clear();

        double totalPrice = 0.0;
        for (Map.Entry<Integer, Integer> entry : newCart.entrySet()) {
            Product product = productService.getProductDetails(entry.getKey());
            int quantity = entry.getValue();

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPriceAtPurchase(product.getPrice());
            existingOrder.addOrderItem(orderItem);

            totalPrice += product.getPrice() * quantity;
        }

        existingOrder.setTotalPrice(totalPrice);
        Order saved = orderRepository.save(existingOrder);
        log.info("Order updated: id={}", orderId);
        return saved;
    }

    @Transactional
    public void deleteOrder(Integer orderId) {
        // Curățare manuală a item-urilor pentru a evita erorile de constrângere pe date vechi
        orderItemRepository.deleteByOrderId(orderId);
        orderRepository.deleteById(orderId);
        log.info("Order and its items deleted: id={}", orderId);
    }
}
