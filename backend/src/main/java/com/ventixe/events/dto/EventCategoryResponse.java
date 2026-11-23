package com.ventixe.events.dto;

public record EventCategoryResponse(
    Long id,
    String name,
    String slug,
    String parentSlug) {
}
