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
@Table(name = "event_categories")
public class EventCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private EventCategory parent;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(nullable = false, unique = true, length = 120)
  private String slug;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  protected EventCategory() {
  }

  public Long getId() {
    return id;
  }

  public EventCategory getParent() {
    return parent;
  }

  public String getName() {
    return name;
  }

  public String getSlug() {
    return slug;
  }

  public boolean isActive() {
    return active;
  }
}
