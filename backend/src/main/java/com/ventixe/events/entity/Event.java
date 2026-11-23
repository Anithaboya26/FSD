package com.ventixe.events.entity;

import com.ventixe.identity.entity.Organizer;
import com.ventixe.identity.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organizer_user_id", nullable = false)
  private Organizer organizer;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private EventCategory category;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "venue_id", nullable = false)
  private Venue venue;

  @Column(nullable = false, length = 240)
  private String title;

  @Column(nullable = false, unique = true, length = 260)
  private String slug;

  @Column(nullable = false, columnDefinition = "mediumtext")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false,
      columnDefinition = "ENUM('DRAFT','PENDING_APPROVAL','PUBLISHED','REJECTED','CANCELLED','COMPLETED','ARCHIVED')")
  private EventStatus status = EventStatus.DRAFT;

  @Column(name = "approval_note", length = 1000)
  private String approvalNote;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by")
  private User reviewer;

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;

  @Column(name = "starts_at", nullable = false)
  private LocalDateTime startsAt;

  @Column(name = "ends_at", nullable = false)
  private LocalDateTime endsAt;

  @Column(nullable = false, length = 64)
  private String timezone;

  @Column(name = "booking_opens_at")
  private LocalDateTime bookingOpensAt;

  @Column(name = "booking_closes_at")
  private LocalDateTime bookingClosesAt;

  @Column(name = "age_restriction")
  private Short ageRestriction;

  @Column(name = "is_featured", nullable = false)
  private boolean featured;

  @Column(name = "published_at")
  private LocalDateTime publishedAt;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private Instant updatedAt;

  protected Event() {
  }

  public Event(
      Organizer organizer,
      EventCategory category,
      Venue venue,
      String title,
      String slug,
      String description,
      LocalDateTime startsAt,
      LocalDateTime endsAt,
      String timezone,
      LocalDateTime bookingOpensAt,
      LocalDateTime bookingClosesAt,
      Short ageRestriction) {
    this.organizer = organizer;
    this.category = category;
    this.venue = venue;
    this.title = title;
    this.slug = slug;
    this.description = description;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.timezone = timezone;
    this.bookingOpensAt = bookingOpensAt;
    this.bookingClosesAt = bookingClosesAt;
    this.ageRestriction = ageRestriction;
    this.status = EventStatus.DRAFT;
  }

  public void updateDraft(
      EventCategory category,
      Venue venue,
      String title,
      String slug,
      String description,
      LocalDateTime startsAt,
      LocalDateTime endsAt,
      String timezone,
      LocalDateTime bookingOpensAt,
      LocalDateTime bookingClosesAt,
      Short ageRestriction) {
    this.category = category;
    this.venue = venue;
    this.title = title;
    this.slug = slug;
    this.description = description;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.timezone = timezone;
    this.bookingOpensAt = bookingOpensAt;
    this.bookingClosesAt = bookingClosesAt;
    this.ageRestriction = ageRestriction;
    this.status = EventStatus.DRAFT;
    this.reviewer = null;
    this.reviewedAt = null;
    this.approvalNote = null;
    this.publishedAt = null;
  }

  public void submitForApproval() {
    if (status != EventStatus.DRAFT && status != EventStatus.REJECTED) {
      throw new IllegalStateException("Only a draft or rejected event can be submitted for approval");
    }
    this.status = EventStatus.PENDING_APPROVAL;
    this.reviewer = null;
    this.reviewedAt = null;
    this.approvalNote = null;
  }

  public void archive() {
    if (status == EventStatus.PUBLISHED || status == EventStatus.COMPLETED) {
      throw new IllegalStateException("Published or completed events cannot be archived by organizers");
    }
    this.status = EventStatus.ARCHIVED;
  }

  public Long getId() {
    return id;
  }

  public Organizer getOrganizer() {
    return organizer;
  }

  public EventCategory getCategory() {
    return category;
  }

  public Venue getVenue() {
    return venue;
  }

  public String getTitle() {
    return title;
  }

  public String getSlug() {
    return slug;
  }

  public String getDescription() {
    return description;
  }

  public EventStatus getStatus() {
    return status;
  }

  public LocalDateTime getStartsAt() {
    return startsAt;
  }

  public LocalDateTime getEndsAt() {
    return endsAt;
  }

  public String getTimezone() {
    return timezone;
  }

  public LocalDateTime getBookingOpensAt() {
    return bookingOpensAt;
  }

  public LocalDateTime getBookingClosesAt() {
    return bookingClosesAt;
  }

  public Short getAgeRestriction() {
    return ageRestriction;
  }

  public boolean isFeatured() {
    return featured;
  }

  public LocalDateTime getPublishedAt() {
    return publishedAt;
  }
}
