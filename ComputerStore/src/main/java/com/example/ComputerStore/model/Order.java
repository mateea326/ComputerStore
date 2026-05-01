package com.example.ComputerStore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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

    // mai multe comenzi pot apartine unui client
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user; // foreign key

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Min(value = 0, message = "Total price has to be positive")
    private double totalPrice;

    // o comanda are mai multe produse (1:M)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // relatie 1:1 cu card
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Card card;

    // metoda pentru a adauga produse
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    // metoda pentru a obtine productQuantities din orderItems
    @Transient // nu se salveaza in bd
    public Map<Integer, Integer> getProductQuantities() {
        if (orderItems == null || orderItems.isEmpty()) {
            return Map.of();
        }
        return orderItems.stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getProductId(),
                        OrderItem::getQuantity // map product id - quantity
                ));
    }
}