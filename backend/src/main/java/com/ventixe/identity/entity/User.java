package com.ventixe.identity.entity;

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
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @Column(nullable = false, unique = true, length = 254)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "ENUM('ACTIVE','SUSPENDED','PENDING_VERIFICATION','DISABLED')")
  private UserStatus status = UserStatus.PENDING_VERIFICATION;

  @Column(name = "email_verified_at")
  private LocalDateTime emailVerifiedAt;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private Instant updatedAt;

  protected User() {
  }

  public User(Role role, String email, String passwordHash) {
    this.role = role;
    this.email = email;
    this.passwordHash = passwordHash;
    this.status = UserStatus.ACTIVE;
  }

  public Long getId() {
    return id;
  }

  public Role getRole() {
    return role;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public UserStatus getStatus() {
    return status;
  }
}
