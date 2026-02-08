package com.example.crudboard.auth.dto;

public record MeResponse(
        Long id,
        String email,
        String role
) {
}
