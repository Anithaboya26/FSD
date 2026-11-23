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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_types")
public class TicketType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "event_id", nullable = false)
  private Event event;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(name = "quantity_total", nullable = false)
  private int quantityTotal;

  @Column(name = "quantity_reserved", nullable = false)
  private int quantityReserved;

  @Column(name = "quantity_sold", nullable = false)
  private int quantitySold;

  @Column(name = "per_order_min", nullable = false)
  private int perOrderMin;

  @Column(name = "per_order_max", nullable = false)
  private int perOrderMax;

  @Column(name = "sales_start_at")
  private LocalDateTime salesStartAt;

  @Column(name = "sales_end_at")
  private LocalDateTime salesEndAt;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private Instant updatedAt;

  protected TicketType() {
  }

  public TicketType(
      Event event,
      String name,
      String description,
      BigDecimal price,
      String currency,
      int quantityTotal,
      int perOrderMin,
      int perOrderMax,
      LocalDateTime salesStartAt,
      LocalDateTime salesEndAt) {
    this.event = event;
    this.name = name;
    this.description = description;
    this.price = price;
    this.currency = currency;
    this.quantityTotal = quantityTotal;
    this.quantityReserved = 0;
    this.quantitySold = 0;
    this.perOrderMin = perOrderMin;
    this.perOrderMax = perOrderMax;
    this.salesStartAt = salesStartAt;
    this.salesEndAt = salesEndAt;
    this.active = true;
  }

  public void updateDefinition(
      String name,
      String description,
      BigDecimal price,
      String currency,
      int quantityTotal,
      int perOrderMin,
      int perOrderMax,
      LocalDateTime salesStartAt,
      LocalDateTime salesEndAt) {
    this.name = name;
    this.description = description;
    this.price = price;
    this.currency = currency;
    this.quantityTotal = quantityTotal;
    this.perOrderMin = perOrderMin;
    this.perOrderMax = perOrderMax;
    this.salesStartAt = salesStartAt;
    this.salesEndAt = salesEndAt;
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }

  public Long getId() {
    return id;
  }

  public Event getEvent() {
    return event;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public String getCurrency() {
    return currency;
  }

  public int getQuantityTotal() {
    return quantityTotal;
  }

  public int getQuantityReserved() {
    return quantityReserved;
  }

  public int getQuantitySold() {
    return quantitySold;
  }

  public int getPerOrderMin() {
    return perOrderMin;
  }

  public int getPerOrderMax() {
    return perOrderMax;
  }

  public LocalDateTime getSalesStartAt() {
    return salesStartAt;
  }

  public LocalDateTime getSalesEndAt() {
    return salesEndAt;
  }

  public boolean isActive() {
    return active;
  }

  public int getAvailableQuantity() {
    return quantityTotal - quantityReserved - quantitySold;
  }
}
