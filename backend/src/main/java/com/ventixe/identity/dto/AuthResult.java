package com.ventixe.identity.dto;

public record AuthResult(
    String token,
    CurrentUserResponse user) {
}
