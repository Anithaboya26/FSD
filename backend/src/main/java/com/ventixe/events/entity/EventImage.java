package com.ventixe.events.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "event_images")
public class EventImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "event_id", nullable = false)
  private Event event;

  @Column(name = "storage_url", nullable = false, length = 1024)
  private String storageUrl;

  @Column(name = "alt_text", length = 255)
  private String altText;

  @Column(name = "sort_order", nullable = false)
  private short sortOrder;

  @Column(name = "is_cover", nullable = false)
  private boolean cover;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  protected EventImage() {
  }

  public Long getId() {
    return id;
  }

  public Event getEvent() {
    return event;
  }

  public String getStorageUrl() {
    return storageUrl;
  }

  public String getAltText() {
    return altText;
  }

  public short getSortOrder() {
    return sortOrder;
  }

  public boolean isCover() {
    return cover;
  }
}
