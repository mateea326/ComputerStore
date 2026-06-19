package com.example.ComputerStore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;

    // relatie M:1 cu User, deoarece mai multe comenzi pot fi facute de acelasi user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // previne buclele infinite de serializare JSON
    private User user;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Min(value = 0, message = "Total price has to be positive")
    private double totalPrice;


    // avem o relatie 1:M cu OrderItem, o comanda contine o lista de obiecte
    // daca stergem o comanda toate obiectele asociate vor fi sterse in cascada
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order() {}

    public Order(int orderId, User user, LocalDateTime orderDate, double totalPrice, List<OrderItem> orderItems) {
        this.orderId = orderId;
        this.user = user;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.orderItems = orderItems;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    @Transient // spune ca aceasta metoda nu trebuie pusa in baza de date si ca e doar o structura ce returneaza id-urile produselor si cantitatilor
    public Map<Integer, Integer> getProductQuantities() {
        if (orderItems == null || orderItems.isEmpty()) {
            return Map.of();
        }
        return orderItems.stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getProductId(),
                        OrderItem::getQuantity
                ));
    }
}