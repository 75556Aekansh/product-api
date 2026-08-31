package com.zestindia.assignment.productapi.dto.response;

import com.zestindia.assignment.productapi.entity.Product;

import java.time.Instant;

public record ProductResponse(
        Long id,
        String productName,
        String createdBy,
        Instant createdOn,
        String modifiedBy,
        Instant modifiedOn
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCreatedBy(),
                product.getCreatedOn(),
                product.getModifiedBy(),
                product.getModifiedOn()
        );
    }
}
