package com.example.ComputerStore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegistrationDTO {

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
    @Email
    @Size(max = 50)
    private String email;

    @NotBlank(message = "Input username")
    @Size(max = 30)
    private String username;

    @NotBlank(message = "Input password")
    @Size(min = 8, max = 50) // Regula din modelul original
    private String password;

    @NotBlank(message = "Confirm your password")
    private String confirmPassword;

    public UserRegistrationDTO() {}

    public UserRegistrationDTO(String firstName, String lastName, String phoneNumber, String address, String email, String username, String password, String confirmPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.email = email;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

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

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}