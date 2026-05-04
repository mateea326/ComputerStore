package com.example.ComputerStore.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String phoneNumber;
    private String address;
}