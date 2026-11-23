package com.ventixe.events.dto;

import java.math.BigDecimal;

public record VenueResponse(
    String name,
    String addressLine1,
    String addressLine2,
    String city,
    String region,
    String postalCode,
    String countryCode,
    BigDecimal latitude,
    BigDecimal longitude,
    String mapUrl) {
}
