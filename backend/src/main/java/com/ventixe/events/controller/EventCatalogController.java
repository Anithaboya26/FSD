
package com.ventixe.events.controller;

import com.ventixe.common.dto.PageResponse;
import com.ventixe.events.dto.EventCategoryResponse;
import com.ventixe.events.dto.EventDetailResponse;
import com.ventixe.events.dto.EventSummaryResponse;
import com.ventixe.events.service.EventCatalogService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class EventCatalogController {

  private final EventCatalogService eventCatalogService;

  public EventCatalogController(EventCatalogService eventCatalogService) {
    this.eventCatalogService = eventCatalogService;
  }

  @GetMapping("/categories")
  public List<EventCategoryResponse> getCategories() {
    return eventCatalogService.getCategories();
  }

  @GetMapping("/events")
  public PageResponse<EventSummaryResponse> searchEvents(
      @RequestParam(required = false) @Size(max = 120) String q,
      @RequestParam(required = false) @Size(max = 120) String city,
      @RequestParam(required = false) @Size(max = 120) String category,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size,
      @RequestParam(defaultValue = "soonest") String sort) {
    return eventCatalogService.search(q, city, category, from, to, page, size, sort);
  }

  @GetMapping("/events/{slug}")
  public EventDetailResponse getEvent(@PathVariable @Size(max = 260) String slug) {
    return eventCatalogService.getPublishedEvent(slug);
  }
}
