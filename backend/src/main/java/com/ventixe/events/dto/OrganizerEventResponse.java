package com.ventixe.events.dto;

import com.ventixe.events.entity.EventStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrganizerEventResponse(
    Long id,
    String slug,
    String title,
    String description,
    EventStatus status,
    Long categoryId,
    LocalDateTime startsAt,
    LocalDateTime endsAt,
    String timezone,
    LocalDateTime bookingOpensAt,
    LocalDateTime bookingClosesAt,
    Short ageRestriction,
    String categoryName,
    VenueResponse venue,
    String venueName,
    String city,
    List<TicketTypeResponse> ticketTypes) {
}
