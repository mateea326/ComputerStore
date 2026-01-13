package com.example.ComputerStore.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id // cheie primara
    @GeneratedValue(strategy = GenerationType.IDENTITY) // valoarea e generata de bd
    private int customerId;

    @NotBlank(message = "Input first name")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Input last name")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "Input phone number")
    @Size(max = 20)
    private String phoneNumber;

    @NotBlank(message = "Input address")
    @Size(max = 100)
    private String address;

    @NotBlank(message = "Input email")
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank(message = "Input username")
    @Size(max = 30)
    private String username;

    @NotBlank(message = "Input password")
    @Size(min = 8, max = 50)
    private String password;

    // un client poate face mai multe comenzi
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Order> orders;
}