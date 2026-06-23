package com.example.ComputerStore.dto;

public class NotificationRequest {
    private String recipient;
    private String type;
    private String message;

    // Getters
    public String getRecipient() { return recipient; }
    public String getType() { return type; }
    public String getMessage() { return message; }

    // Setters
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public void setType(String type) { this.type = type; }
    public void setMessage(String message) { this.message = message; }
}
