package com.zestindia.assignment.productapi.dto.response;

import com.zestindia.assignment.productapi.entity.Item;

public record ItemResponse(
        Long id,
        Integer quantity
) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getQuantity()
        );
    }
}