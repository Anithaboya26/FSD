package com.ventixe.events.service;

import com.ventixe.common.exception.ResourceNotFoundException;
import com.ventixe.events.dto.CreateEventRequest;
import com.ventixe.events.dto.OrganizerEventResponse;
import com.ventixe.events.dto.TicketTypeRequest;
import com.ventixe.events.dto.TicketTypeResponse;
import com.ventixe.events.dto.VenueResponse;
import com.ventixe.events.dto.VenueRequest;
import com.ventixe.events.entity.Event;
import com.ventixe.events.entity.EventCategory;
import com.ventixe.events.entity.EventStatus;
import com.ventixe.events.entity.TicketType;
import com.ventixe.events.entity.Venue;
import com.ventixe.events.repository.EventCategoryRepository;
import com.ventixe.events.repository.EventRepository;
import com.ventixe.events.repository.TicketTypeRepository;
import com.ventixe.events.repository.VenueRepository;
import com.ventixe.identity.entity.Organizer;
import com.ventixe.identity.entity.OrganizerVerificationStatus;
import com.ventixe.identity.repository.OrganizerRepository;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizerEventService {

  private final EventRepository eventRepository;
  private final EventCategoryRepository categoryRepository;
  private final VenueRepository venueRepository;
  private final TicketTypeRepository ticketTypeRepository;
  private final OrganizerRepository organizerRepository;

  public OrganizerEventService(
      EventRepository eventRepository,
      EventCategoryRepository categoryRepository,
      VenueRepository venueRepository,
      TicketTypeRepository ticketTypeRepository,
      OrganizerRepository organizerRepository) {
    this.eventRepository = eventRepository;
    this.categoryRepository = categoryRepository;
    this.venueRepository = venueRepository;
    this.ticketTypeRepository = ticketTypeRepository;
    this.organizerRepository = organizerRepository;
  }

  @Transactional(readOnly = true)
  public List<OrganizerEventResponse> listMine(long organizerUserId) {
    return eventRepository.findByOrganizer_UserIdOrderByCreatedAtDesc(organizerUserId).stream()
        .map(event -> toResponse(event,
            ticketTypeRepository.findByEvent_IdAndActiveTrueOrderByPriceAsc(event.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public OrganizerEventResponse getMine(long organizerUserId, long eventId) {
    Event event = findOwnedEvent(organizerUserId, eventId);
    return toResponse(event, ticketTypeRepository.findByEvent_IdAndActiveTrueOrderByPriceAsc(eventId));
  }

  @Transactional
  public OrganizerEventResponse create(long organizerUserId, CreateEventRequest request) {
    Organizer organizer = findEligibleOrganizer(organizerUserId);
    validateEvent(request);
    EventCategory category = findCategory(request.categoryId());
    Venue venue = venueRepository.save(toVenue(request.venue()));
    Event event = new Event(
        organizer,
        category,
        venue,
        request.title().trim(),
        createSlug(request.title()),
        request.description().trim(),
        request.startsAt(),
        request.endsAt(),
        request.timezone(),
        request.bookingOpensAt(),
        request.bookingClosesAt(),
        request.ageRestriction());
    Event savedEvent = eventRepository.save(event);

    List<TicketType> ticketTypes = request.ticketTypes().stream()
        .map(ticket -> new TicketType(
            savedEvent,
            ticket.name().trim(),
            normalizeOptional(ticket.description()),
            ticket.price(),
            ticket.currency().toUpperCase(Locale.ROOT),
            ticket.quantityTotal(),
            ticket.perOrderMin(),
            ticket.perOrderMax(),
            ticket.salesStartAt(),
            ticket.salesEndAt()))
        .toList();
    return toResponse(savedEvent, ticketTypeRepository.saveAll(ticketTypes));
  }

  @Transactional
  public OrganizerEventResponse update(
      long organizerUserId,
      long eventId,
      CreateEventRequest request) {
    findEligibleOrganizer(organizerUserId);
    Event event = findOwnedEvent(organizerUserId, eventId);
    if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.REJECTED) {
      throw new IllegalStateException("Only draft or rejected events can be edited");
    }
    validateEvent(request);
    List<TicketType> existingTypes = ticketTypeRepository.findByEvent_IdOrderByPriceAsc(eventId);
    boolean hasInventoryActivity = existingTypes.stream()
        .anyMatch(ticket -> ticket.getQuantitySold() > 0 || ticket.getQuantityReserved() > 0);
    if (hasInventoryActivity) {
      throw new IllegalStateException("Events with reserved or sold tickets cannot be edited");
    }

    EventCategory category = findCategory(request.categoryId());
    Venue venue = event.getVenue();
    Venue updatedVenue = toVenue(request.venue());
    venue.update(
        updatedVenue.getName(),
        updatedVenue.getAddressLine1(),
        updatedVenue.getAddressLine2(),
        updatedVenue.getCity(),
        updatedVenue.getRegion(),
        updatedVenue.getPostalCode(),
        updatedVenue.getCountryCode(),
        updatedVenue.getLatitude(),
        updatedVenue.getLongitude(),
        updatedVenue.getMapUrl());
    event.updateDraft(
        category,
        venue,
        request.title().trim(),
        createSlug(request.title()),
        request.description().trim(),
        request.startsAt(),
        request.endsAt(),
        request.timezone(),
        request.bookingOpensAt(),
        request.bookingClosesAt(),
        request.ageRestriction());
    updateTicketTypes(event, existingTypes, request.ticketTypes());
    return toResponse(event, ticketTypeRepository.findByEvent_IdAndActiveTrueOrderByPriceAsc(eventId));
  }

  @Transactional
  public OrganizerEventResponse submit(long organizerUserId, long eventId) {
    findEligibleOrganizer(organizerUserId);
    Event event = findOwnedEvent(organizerUserId, eventId);
    if (ticketTypeRepository.findByEvent_IdAndActiveTrueOrderByPriceAsc(eventId).isEmpty()) {
      throw new IllegalStateException("Add at least one ticket type before submitting");
    }
    event.submitForApproval();
    return toResponse(event, ticketTypeRepository.findByEvent_IdAndActiveTrueOrderByPriceAsc(eventId));
  }

  @Transactional
  public void archive(long organizerUserId, long eventId) {
    findEligibleOrganizer(organizerUserId);
    Event event = findOwnedEvent(organizerUserId, eventId);
    event.archive();
  }

  private void updateTicketTypes(
      Event event,
      List<TicketType> existingTypes,
      List<TicketTypeRequest> requestedTypes) {
    HashSet<TicketType> retainedTypes = new HashSet<>();
    List<TicketType> newTypes = new ArrayList<>();
    for (TicketTypeRequest request : requestedTypes) {
      TicketType existing = existingTypes.stream()
          .filter(ticket -> ticket.getName().equalsIgnoreCase(request.name().trim()))
          .findFirst()
          .orElse(null);
      if (existing == null) {
        newTypes.add(new TicketType(
            event,
            request.name().trim(),
            normalizeOptional(request.description()),
            request.price(),
            request.currency().toUpperCase(Locale.ROOT),
            request.quantityTotal(),
            request.perOrderMin(),
            request.perOrderMax(),
            request.salesStartAt(),
            request.salesEndAt()));
      } else {
        retainedTypes.add(existing);
        existing.updateDefinition(
            request.name().trim(),
            normalizeOptional(request.description()),
            request.price(),
            request.currency().toUpperCase(Locale.ROOT),
            request.quantityTotal(),
            request.perOrderMin(),
            request.perOrderMax(),
            request.salesStartAt(),
            request.salesEndAt());
      }
    }
    existingTypes.stream()
        .filter(TicketType::isActive)
        .filter(ticket -> !retainedTypes.contains(ticket))
        .forEach(TicketType::deactivate);
    if (!newTypes.isEmpty()) {
      ticketTypeRepository.saveAll(newTypes);
    }
  }

  private void validateEvent(CreateEventRequest request) {
    if (!request.endsAt().isAfter(request.startsAt())) {
      throw new IllegalArgumentException("endsAt must be after startsAt");
    }
    try {
      ZoneId.of(request.timezone());
    } catch (DateTimeException exception) {
      throw new IllegalArgumentException("timezone must be a valid IANA timezone");
    }
    if (request.bookingOpensAt() != null
        && request.bookingClosesAt() != null
        && !request.bookingClosesAt().isAfter(request.bookingOpensAt())) {
      throw new IllegalArgumentException("bookingClosesAt must be after bookingOpensAt");
    }
    if (request.bookingClosesAt() != null && request.bookingClosesAt().isAfter(request.startsAt())) {
      throw new IllegalArgumentException("bookingClosesAt must be at or before startsAt");
    }
    if (request.bookingOpensAt() != null && request.bookingOpensAt().isAfter(request.startsAt())) {
      throw new IllegalArgumentException("bookingOpensAt must be at or before startsAt");
    }
    LocalDateTime now = LocalDateTime.now(ZoneId.of(request.timezone()));
    if (request.startsAt().isBefore(now)) {
      throw new IllegalArgumentException("startsAt must be in the future");
    }
    if (request.bookingOpensAt() != null && request.bookingOpensAt().isBefore(now)) {
      throw new IllegalArgumentException("bookingOpensAt must be in the future");
    }
    if (request.ticketTypes() == null || request.ticketTypes().isEmpty()) {
      throw new IllegalArgumentException("At least one ticket type is required");
    }

    HashSet<String> names = new HashSet<>();
    String currency = request.ticketTypes().getFirst().currency().toUpperCase(Locale.ROOT);
    for (TicketTypeRequest ticket : request.ticketTypes()) {
      String normalizedName = ticket.name().trim().toLowerCase(Locale.ROOT);
      if (!names.add(normalizedName)) {
        throw new IllegalArgumentException("Ticket type names must be unique within an event");
      }
      if (!currency.equals(ticket.currency().toUpperCase(Locale.ROOT))) {
        throw new IllegalArgumentException("All ticket types for an event must use the same currency");
      }
      if (ticket.perOrderMin() < 1 || ticket.perOrderMax() < ticket.perOrderMin()) {
        throw new IllegalArgumentException("Ticket order limits must satisfy 1 <= perOrderMin <= perOrderMax");
      }
      if (ticket.perOrderMax() > ticket.quantityTotal()) {
        throw new IllegalArgumentException("perOrderMax cannot exceed ticket quantity");
      }
      if (ticket.salesStartAt() != null && ticket.salesStartAt().isBefore(now)) {
        throw new IllegalArgumentException("Ticket salesStartAt must be in the future");
      }
      if (ticket.salesStartAt() != null && ticket.salesStartAt().isAfter(request.startsAt())) {
        throw new IllegalArgumentException("Ticket salesStartAt must be at or before startsAt");
      }
      if (ticket.salesEndAt() != null && ticket.salesEndAt().isAfter(request.startsAt())) {
        throw new IllegalArgumentException("Ticket salesEndAt must be at or before startsAt");
      }
      if (ticket.salesStartAt() != null
          && ticket.salesEndAt() != null
          && !ticket.salesEndAt().isAfter(ticket.salesStartAt())) {
        throw new IllegalArgumentException("Ticket salesEndAt must be after salesStartAt");
      }
    }
  }

  private Organizer findEligibleOrganizer(long organizerUserId) {
    Organizer organizer = organizerRepository.findById(organizerUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Organizer account not found"));
    if (organizer.getVerificationStatus() == OrganizerVerificationStatus.SUSPENDED) {
      throw new IllegalStateException("Suspended organizers cannot manage events");
    }
    return organizer;
  }

  private Event findOwnedEvent(long organizerUserId, long eventId) {
    return eventRepository.findByIdAndOrganizer_UserId(eventId, organizerUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
  }

  private EventCategory findCategory(long categoryId) {
    return categoryRepository.findByIdAndActiveTrue(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Active event category not found"));
  }

  private Venue toVenue(VenueRequest request) {
    if (request.latitude() == null ^ request.longitude() == null) {
      throw new IllegalArgumentException("Latitude and longitude must be provided together");
    }
    String mapUrl = request.mapUrl() == null ? null : request.mapUrl().trim();
    if (mapUrl != null && !mapUrl.isEmpty()) {
      java.net.URI uri;
      try {
        uri = java.net.URI.create(mapUrl);
      } catch (IllegalArgumentException exception) {
        throw new IllegalArgumentException("mapUrl must be a valid HTTPS URL");
      }
      if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
        throw new IllegalArgumentException("mapUrl must be a valid HTTPS URL");
      }
    } else {
      mapUrl = null;
    }
    return new Venue(
        request.name().trim(),
        request.addressLine1().trim(),
        normalizeOptional(request.addressLine2()),
        request.city().trim(),
        normalizeOptional(request.region()),
        normalizeOptional(request.postalCode()),
        request.countryCode().toUpperCase(Locale.ROOT),
        request.latitude(),
        request.longitude(),
        mapUrl);
  }

  private OrganizerEventResponse toResponse(Event event, List<TicketType> ticketTypes) {
    return new OrganizerEventResponse(
        event.getId(),
        event.getSlug(),
        event.getTitle(),
        event.getDescription(),
        event.getStatus(),
        event.getCategory().getId(),
        event.getStartsAt(),
        event.getEndsAt(),
        event.getTimezone(),
        event.getBookingOpensAt(),
        event.getBookingClosesAt(),
        event.getAgeRestriction(),
        event.getCategory().getName(),
        toVenueResponse(event.getVenue()),
        event.getVenue().getName(),
        event.getVenue().getCity(),
        ticketTypes.stream()
            .map(ticket -> new TicketTypeResponse(
                ticket.getId(),
                ticket.getName(),
                ticket.getDescription(),
                ticket.getPrice(),
                ticket.getCurrency(),
                ticket.getAvailableQuantity(),
                ticket.getPerOrderMin(),
                ticket.getPerOrderMax(),
                ticket.getSalesStartAt(),
                ticket.getSalesEndAt()))
            .toList());
  }

  private VenueResponse toVenueResponse(Venue venue) {
    return new VenueResponse(
        venue.getName(),
        venue.getAddressLine1(),
        venue.getAddressLine2(),
        venue.getCity(),
        venue.getRegion(),
        venue.getPostalCode(),
        venue.getCountryCode(),
        venue.getLatitude(),
        venue.getLongitude(),
        venue.getMapUrl());
  }

  private String createSlug(String title) {
    String normalized = Normalizer.normalize(title, Normalizer.Form.NFKD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("(^-+|-+$)", "");
    String prefix = normalized.isBlank() ? "event" : normalized;
    prefix = prefix.substring(0, Math.min(prefix.length(), 230));
    String slug;
    do {
      slug = prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    } while (eventRepository.existsBySlug(slug));
    return slug;
  }

  private String normalizeOptional(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
