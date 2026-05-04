package com.example.ComputerStore.exception;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException(String message) {
        super(message);
    }

    public EmptyCartException() {
        super("Cart is empty");
    }
}
