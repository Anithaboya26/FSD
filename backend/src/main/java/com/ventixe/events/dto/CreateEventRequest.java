package com.ventixe.events.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record CreateEventRequest(
    @NotBlank @Size(max = 240) String title,
    @NotBlank @Size(min = 20, max = 20000) String description,
    @NotNull @Positive Long categoryId,
    @NotNull LocalDateTime startsAt,
    @NotNull LocalDateTime endsAt,
    @NotBlank @Size(max = 64) String timezone,
    LocalDateTime bookingOpensAt,
    LocalDateTime bookingClosesAt,
    @jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(100) Short ageRestriction,
    @NotNull @Valid VenueRequest venue,
    @NotNull @Size(min = 1, max = 10) List<@NotNull @Valid TicketTypeRequest> ticketTypes) {
}
