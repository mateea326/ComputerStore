package com.example.ComputerStore.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int customerId;

    @NotBlank(message = "Input first name")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Input last name")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Input phone number")
    @Size(max = 100)
    private String phoneNumber;

    @NotBlank(message = "Input address")
    @Size(max = 100)
    private String address;

    @NotBlank(message = "Input email")
    @Size(max = 100)
    @Email
    private String email;

    @NotBlank(message = "Input username")
    @Size(max = 100)
    private String username;

    @NotBlank(message = "Input password")
    @Size(min = 8, max = 100)
    private String password;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Order> orders;
}