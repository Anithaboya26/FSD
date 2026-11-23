package com.ventixe.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(min = 12, max = 72) String password,
    @NotBlank @Size(max = 100) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @NotBlank @Pattern(regexp = "CUSTOMER|ORGANIZER") String role,
    @Size(max = 200) String businessName) {
}
