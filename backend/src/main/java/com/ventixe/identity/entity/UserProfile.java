package com.ventixe.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

  @Id
  @Column(name = "user_id")
  private Long userId;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(length = 32, unique = true)
  private String phone;

  @Column(name = "avatar_url", length = 1024)
  private String avatarUrl;

  @Column(name = "preferred_city", length = 120)
  private String preferredCity;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private Instant updatedAt;

  protected UserProfile() {
  }

  public UserProfile(User user, String firstName, String lastName) {
    this.user = user;
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
