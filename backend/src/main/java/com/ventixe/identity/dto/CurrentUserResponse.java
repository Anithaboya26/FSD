package com.ventixe.identity.dto;

public record CurrentUserResponse(
    Long id,
    String email,
    String role) {
}
