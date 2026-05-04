package com.example.ComputerStore.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Integer id) {
        super(resourceName + " with id " + id + " was not found");
    }
}
