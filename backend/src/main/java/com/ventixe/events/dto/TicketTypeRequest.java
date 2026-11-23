package com.ventixe.events.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketTypeRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description,
    @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
    @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currency,
    @Positive int quantityTotal,
    @Positive int perOrderMin,
    @Positive int perOrderMax,
    LocalDateTime salesStartAt,
    LocalDateTime salesEndAt) {
}
