package com.zestindia.assignment.productapi.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
}