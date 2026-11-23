package com.ventixe.security;

public record UserPrincipal(
    long id,
    String email,
    String role) {
}
