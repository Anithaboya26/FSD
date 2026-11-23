package com.ventixe.events.dto;

public record EventImageResponse(
    String url,
    String altText,
    int sortOrder,
    boolean cover) {
}
