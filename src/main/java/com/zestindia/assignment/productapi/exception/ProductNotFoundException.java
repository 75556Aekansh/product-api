package com.zestindia.assignment.productapi.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product with id %d was not found".formatted(productId));
    }
}
