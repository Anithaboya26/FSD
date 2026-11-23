package com.ventixe.events.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventDetailResponse(
    EventSummaryResponse event,
    String description,
    LocalDateTime bookingOpensAt,
    LocalDateTime bookingClosesAt,
    VenueResponse venue,
    List<EventImageResponse> images,
    List<TicketTypeResponse> ticketTypes) {
}
