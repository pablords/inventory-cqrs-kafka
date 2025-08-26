package com.pablords.command.exception;

public enum ErrorMessages {
    PRODUCT_NOT_FOUND("Product not found"),
    INSUFFICIENT_STOCK("Not enough stock");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
