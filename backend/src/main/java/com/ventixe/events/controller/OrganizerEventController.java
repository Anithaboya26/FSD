package com.ventixe.events.controller;

import com.ventixe.events.dto.CreateEventRequest;
import com.ventixe.events.dto.OrganizerEventResponse;
import com.ventixe.events.service.OrganizerEventService;
import com.ventixe.security.UserPrincipal;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/organizer/events", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrganizerEventController {

  private final OrganizerEventService organizerEventService;

  public OrganizerEventController(OrganizerEventService organizerEventService) {
    this.organizerEventService = organizerEventService;
  }

  @GetMapping
  public List<OrganizerEventResponse> listMine(Authentication authentication) {
    return organizerEventService.listMine(principal(authentication).id());
  }

  @GetMapping("/{eventId}")
  public OrganizerEventResponse getMine(
      @PathVariable long eventId,
      Authentication authentication) {
    return organizerEventService.getMine(principal(authentication).id(), eventId);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<OrganizerEventResponse> create(
      @Valid @RequestBody CreateEventRequest request,
      Authentication authentication) {
    OrganizerEventResponse response =
        organizerEventService.create(principal(authentication).id(), request);
    return ResponseEntity.created(URI.create("/api/v1/organizer/events/" + response.id()))
        .body(response);
  }

  @PutMapping(path = "/{eventId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public OrganizerEventResponse update(
      @PathVariable long eventId,
      @Valid @RequestBody CreateEventRequest request,
      Authentication authentication) {
    return organizerEventService.update(principal(authentication).id(), eventId, request);
  }

  @PostMapping("/{eventId}/submit")
  public OrganizerEventResponse submit(
      @PathVariable long eventId,
      Authentication authentication) {
    return organizerEventService.submit(principal(authentication).id(), eventId);
  }

  @DeleteMapping("/{eventId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void archive(
      @PathVariable long eventId,
      Authentication authentication) {
    organizerEventService.archive(principal(authentication).id(), eventId);
  }

  private UserPrincipal principal(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal user)) {
      throw new IllegalStateException("Authenticated organizer is required");
    }
    return user;
  }
}
