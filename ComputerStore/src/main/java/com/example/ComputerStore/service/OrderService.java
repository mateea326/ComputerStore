package com.example.ComputerStore.service;

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

    public OrderService(OrderRepository orderRepository,
                        UserService userService,
                        ProductService productService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
    }

    public List<Order> getOrderHistory(Integer userId) {
        User user = userService.findUserById(userId);
        return orderRepository.findByUser(user);
    }

    public Page<Order> getOrderHistory(Integer userId, Pageable pageable) {
        User user = userService.findUserById(userId);
        return orderRepository.findByUser(user, pageable);
    }

    public Order createOrder(Integer userId, Map<Integer, Integer> cart) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        User user = userService.findUserById(userId);

        // creare comanda
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());

        double totalPrice = 0.0;

        // creare OrderItems
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

        return orderRepository.save(order);
    }

    // creare comanda cu card (folosit de SessionCartService la checkout)
    // creare comanda (fostul createOrderWithCard, acum doar creeaza comanda)
    public Order createOrderWithPaymentDetails(Integer userId, Map<Integer, Integer> cart) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        User user = userService.findUserById(userId);

        // creare comanda
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());

        double totalPrice = 0.0;

        // creare OrderItems
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

        return orderRepository.save(order);
    }

    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order with id " + orderId + " not found"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    // comanda update order
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

    // comanda delete order
    public void deleteOrder(Integer orderId) {
        Order order = getOrderById(orderId);
        orderRepository.delete(order);
        log.info("Order deleted: id={}", orderId);
    }
}