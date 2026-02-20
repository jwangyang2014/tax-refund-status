package com.intuit.taxrefund.auth.api.dto;

public record MeResponse(
        Long userId,
        String email,
        String password
) {
}
