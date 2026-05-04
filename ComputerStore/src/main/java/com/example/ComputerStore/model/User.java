package com.example.ComputerStore.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

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
    @Size(min = 8, max = 255)
    private String password;

    @Column(nullable = true)
    private String role = "USER";

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Order> orders;

    // relatie 1:1 cu Cart
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Cart cart;

    // relatie 1:1 cu Wishlist
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Wishlist wishlist;

    public User() {}

    public User(int userId, String firstName, String lastName, String phoneNumber, String address, String email, String username, String password) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public Wishlist getWishlist() { return wishlist; }
    public void setWishlist(Wishlist wishlist) { this.wishlist = wishlist; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}