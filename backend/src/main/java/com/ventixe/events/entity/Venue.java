package com.ventixe.events.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "venues")
public class Venue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(name = "address_line1", nullable = false, length = 255)
  private String addressLine1;

  @Column(name = "address_line2", length = 255)
  private String addressLine2;

  @Column(nullable = false, length = 120)
  private String city;

  @Column(length = 120)
  private String region;

  @Column(name = "postal_code", length = 24)
  private String postalCode;

  @Column(name = "country_code", nullable = false, length = 2)
  private String countryCode;

  @Column(precision = 10, scale = 7)
  private BigDecimal latitude;

  @Column(precision = 10, scale = 7)
  private BigDecimal longitude;

  @Column(name = "map_url", length = 1024)
  private String mapUrl;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private Instant updatedAt;

  protected Venue() {
  }

  public Venue(
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
    this.name = name;
    this.addressLine1 = addressLine1;
    this.addressLine2 = addressLine2;
    this.city = city;
    this.region = region;
    this.postalCode = postalCode;
    this.countryCode = countryCode;
    this.latitude = latitude;
    this.longitude = longitude;
    this.mapUrl = mapUrl;
  }

  public void update(
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
    this.name = name;
    this.addressLine1 = addressLine1;
    this.addressLine2 = addressLine2;
    this.city = city;
    this.region = region;
    this.postalCode = postalCode;
    this.countryCode = countryCode;
    this.latitude = latitude;
    this.longitude = longitude;
    this.mapUrl = mapUrl;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getAddressLine1() {
    return addressLine1;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public String getCity() {
    return city;
  }

  public String getRegion() {
    return region;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public String getMapUrl() {
    return mapUrl;
  }
}
