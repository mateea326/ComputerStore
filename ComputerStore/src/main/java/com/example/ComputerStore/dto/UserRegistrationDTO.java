package com.example.ComputerStore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
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
}