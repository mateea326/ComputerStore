package com.example.ComputerStore.dto;

import lombok.Data;

@Data
public class NotificationRequest {
    private String recipient;
    private String type;
    private String message;
}
