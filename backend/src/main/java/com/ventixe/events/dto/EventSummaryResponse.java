package com.ventixe.events.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventSummaryResponse(
    Long id,
    String slug,
    String title,
    String description,
    LocalDateTime startsAt,
    LocalDateTime endsAt,
    String timezone,
    String categoryName,
    String categorySlug,
    String venueName,
    String city,
    String countryCode,
    String coverImageUrl,
    BigDecimal minimumTicketPrice,
    String currency,
    boolean featured) {
}
