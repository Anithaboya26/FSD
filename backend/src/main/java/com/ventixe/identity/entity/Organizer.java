package com.ventixe.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizers")
public class Organizer {

  @Id
  @Column(name = "user_id")
  private Long userId;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "business_name", nullable = false, length = 200)
  private String businessName;

  @Column(name = "business_email", length = 254)
  private String businessEmail;

  @Column(name = "business_phone", length = 32)
  private String businessPhone;

  @Enumerated(EnumType.STRING)
  @Column(name = "verification_status", nullable = false,
      columnDefinition = "ENUM('PENDING','APPROVED','REJECTED','SUSPENDED')")
  private OrganizerVerificationStatus verificationStatus = OrganizerVerificationStatus.PENDING;

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;

  @Column(name = "review_note", length = 1000)
  private String reviewNote;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private Instant updatedAt;

  protected Organizer() {
  }

  public Organizer(User user, String businessName) {
    this.user = user;
    this.businessName = businessName;
    this.verificationStatus = OrganizerVerificationStatus.PENDING;
  }

  public Long getUserId() {
    return userId;
  }

  public User getUser() {
    return user;
  }

  public String getBusinessName() {
    return businessName;
  }

  public OrganizerVerificationStatus getVerificationStatus() {
    return verificationStatus;
  }
}
