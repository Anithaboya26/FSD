package com.ventixe.events.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketTypeResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    String currency,
    int availableQuantity,
    int perOrderMin,
    int perOrderMax,
    LocalDateTime salesStartAt,
    LocalDateTime salesEndAt) {
}
