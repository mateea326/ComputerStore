package com.example.ComputerStore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cardId;

    @Size(min = 10, max = 20)
    @NotBlank(message = "Input card number")
    private String cardNumber;

    @Size(min = 5, max = 100)
    @NotBlank(message = "Input card name")
    private String cardName;

    @Size(min = 5, max = 20)
    @NotBlank(message = "Input expiry date")
    private String expiryDate;

    @Size(min = 3, max = 4)
    @NotBlank(message = "Input CVV")
    private String CVV;

    @OneToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order; // foreign key -> un card e asociat unei comenzi
}