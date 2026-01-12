package com.example.ComputerStore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Min(value = 0, message = "Total price has to be positive")
    private double totalPrice;

    // Relație cu OrderItem (ONE Order -> MANY OrderItems)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // Relație cu Card (ONE Order -> ONE Card)
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Card card;

    // Helper method pentru a adăuga items
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    // Helper method pentru a obține productQuantities din orderItems
    @Transient
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