package com.zestindia.assignment.productapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
        @NotBlank(message = "productName is required")
        @Size(max = 255, message = "productName must not exceed 255 characters")
        String productName
) {
}
