-- VENTIXE Phase 1 baseline schema
-- Target: MySQL 8.0.16+ (InnoDB, enforced CHECK constraints)
-- Apply to a newly created database. Application secrets and demo users are
-- intentionally not seeded here.

CREATE DATABASE IF NOT EXISTS ventixe
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE ventixe;

CREATE TABLE roles (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(32) NOT NULL,
  description VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB;

CREATE TABLE users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_id BIGINT UNSIGNED NOT NULL,
  email VARCHAR(254) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  status ENUM('ACTIVE','SUSPENDED','PENDING_VERIFICATION','DISABLED') NOT NULL DEFAULT 'PENDING_VERIFICATION',
  email_verified_at DATETIME(6) NULL,
  last_login_at DATETIME(6) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email),
  KEY idx_users_role_status (role_id, status),
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE user_profiles (
  user_id BIGINT UNSIGNED NOT NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  phone VARCHAR(32) NULL,
  avatar_url VARCHAR(1024) NULL,
  preferred_city VARCHAR(120) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_user_profiles_phone (phone),
  CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE organizers (
  user_id BIGINT UNSIGNED NOT NULL,
  business_name VARCHAR(200) NOT NULL,
  business_email VARCHAR(254) NULL,
  business_phone VARCHAR(32) NULL,
  verification_status ENUM('PENDING','APPROVED','REJECTED','SUSPENDED') NOT NULL DEFAULT 'PENDING',
  reviewed_by BIGINT UNSIGNED NULL,
  reviewed_at DATETIME(6) NULL,
  review_note VARCHAR(1000) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  KEY idx_organizers_status_created (verification_status, created_at),
  CONSTRAINT fk_organizers_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT fk_organizers_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE organizer_verifications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  organizer_user_id BIGINT UNSIGNED NOT NULL,
  document_type VARCHAR(80) NOT NULL,
  storage_url VARCHAR(1024) NOT NULL,
  status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  submitted_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  reviewed_by BIGINT UNSIGNED NULL,
  reviewed_at DATETIME(6) NULL,
  review_note VARCHAR(1000) NULL,
  PRIMARY KEY (id),
  KEY idx_verifications_organizer_status (organizer_user_id, status),
  CONSTRAINT fk_verifications_organizer FOREIGN KEY (organizer_user_id) REFERENCES organizers (user_id) ON DELETE RESTRICT,
  CONSTRAINT fk_verifications_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE event_categories (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  parent_id BIGINT UNSIGNED NULL,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(120) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_event_categories_name (name),
  UNIQUE KEY uk_event_categories_slug (slug),
  KEY idx_event_categories_parent (parent_id),
  CONSTRAINT fk_event_categories_parent FOREIGN KEY (parent_id) REFERENCES event_categories (id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE venues (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  address_line1 VARCHAR(255) NOT NULL,
  address_line2 VARCHAR(255) NULL,
  city VARCHAR(120) NOT NULL,
  region VARCHAR(120) NULL,
  postal_code VARCHAR(24) NULL,
  country_code CHAR(2) NOT NULL,
  latitude DECIMAL(10,7) NULL,
  longitude DECIMAL(10,7) NULL,
  map_url VARCHAR(1024) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_venues_city_country (city, country_code),
  CONSTRAINT chk_venues_latitude CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
  CONSTRAINT chk_venues_longitude CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
) ENGINE=InnoDB;

CREATE TABLE events (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  organizer_user_id BIGINT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED NOT NULL,
  venue_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(240) NOT NULL,
  slug VARCHAR(260) NOT NULL,
  description MEDIUMTEXT NOT NULL,
  status ENUM('DRAFT','PENDING_APPROVAL','PUBLISHED','REJECTED','CANCELLED','COMPLETED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  approval_note VARCHAR(1000) NULL,
  reviewed_by BIGINT UNSIGNED NULL,
  reviewed_at DATETIME(6) NULL,
  starts_at DATETIME(6) NOT NULL,
  ends_at DATETIME(6) NOT NULL,
  timezone VARCHAR(64) NOT NULL,
  booking_opens_at DATETIME(6) NULL,
  booking_closes_at DATETIME(6) NULL,
  age_restriction TINYINT UNSIGNED NULL,
  is_featured BOOLEAN NOT NULL DEFAULT FALSE,
  published_at DATETIME(6) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_events_slug (slug),
  KEY idx_events_discovery (status, starts_at, category_id),
  KEY idx_events_organizer (organizer_user_id, status, starts_at),
  KEY idx_events_venue (venue_id),
  KEY idx_events_featured (is_featured, status, starts_at),
  CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_user_id) REFERENCES organizers (user_id) ON DELETE RESTRICT,
  CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES event_categories (id) ON DELETE RESTRICT,
  CONSTRAINT fk_events_venue FOREIGN KEY (venue_id) REFERENCES venues (id) ON DELETE RESTRICT,
  CONSTRAINT fk_events_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (id) ON DELETE SET NULL,
  CONSTRAINT chk_events_time_order CHECK (ends_at > starts_at),
  CONSTRAINT chk_events_booking_window CHECK (booking_closes_at IS NULL OR booking_opens_at IS NULL OR booking_closes_at > booking_opens_at),
  CONSTRAINT chk_events_age_restriction CHECK (age_restriction IS NULL OR age_restriction <= 100)
) ENGINE=InnoDB;

CREATE TABLE event_images (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  event_id BIGINT UNSIGNED NOT NULL,
  storage_url VARCHAR(1024) NOT NULL,
  alt_text VARCHAR(255) NULL,
  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  is_cover BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_event_images_order (event_id, sort_order),
  CONSTRAINT fk_event_images_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE ticket_types (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  event_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500) NULL,
  price DECIMAL(12,2) NOT NULL,
  currency CHAR(3) NOT NULL,
  quantity_total INT UNSIGNED NOT NULL,
  quantity_reserved INT UNSIGNED NOT NULL DEFAULT 0,
  quantity_sold INT UNSIGNED NOT NULL DEFAULT 0,
  per_order_min SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  per_order_max SMALLINT UNSIGNED NOT NULL DEFAULT 10,
  sales_start_at DATETIME(6) NULL,
  sales_end_at DATETIME(6) NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ticket_types_event_name (event_id, name),
  KEY idx_ticket_types_event_active (event_id, is_active),
  CONSTRAINT fk_ticket_types_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE RESTRICT,
  CONSTRAINT chk_ticket_types_price CHECK (price >= 0),
  CONSTRAINT chk_ticket_types_inventory CHECK (quantity_reserved + quantity_sold <= quantity_total),
  CONSTRAINT chk_ticket_types_order_limits CHECK (per_order_min >= 1 AND per_order_max >= per_order_min),
  CONSTRAINT chk_ticket_types_sales_window CHECK (sales_end_at IS NULL OR sales_start_at IS NULL OR sales_end_at > sales_start_at)
) ENGINE=InnoDB;

CREATE TABLE coupons (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  discount_type ENUM('PERCENT','FIXED') NOT NULL,
  discount_value DECIMAL(12,2) NOT NULL,
  currency CHAR(3) NULL,
  minimum_purchase DECIMAL(12,2) NOT NULL DEFAULT 0,
  maximum_discount DECIMAL(12,2) NULL,
  starts_at DATETIME(6) NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  usage_limit INT UNSIGNED NULL,
  per_user_limit SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_by BIGINT UNSIGNED NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_coupons_code (code),
  KEY idx_coupons_active_window (is_active, starts_at, expires_at),
  CONSTRAINT fk_coupons_creator FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
  CONSTRAINT chk_coupons_value CHECK (discount_value > 0),
  CONSTRAINT chk_coupons_minimum CHECK (minimum_purchase >= 0),
  CONSTRAINT chk_coupons_max_discount CHECK (maximum_discount IS NULL OR maximum_discount > 0),
  CONSTRAINT chk_coupons_window CHECK (expires_at > starts_at),
  CONSTRAINT chk_coupons_percent CHECK (discount_type <> 'PERCENT' OR discount_value <= 100)
) ENGINE=InnoDB;

CREATE TABLE bookings (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  booking_code CHAR(26) NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  event_id BIGINT UNSIGNED NOT NULL,
  coupon_id BIGINT UNSIGNED NULL,
  status ENUM('PENDING_PAYMENT','CONFIRMED','CANCELLED','EXPIRED','REFUND_PENDING','PARTIALLY_REFUNDED','REFUNDED') NOT NULL DEFAULT 'PENDING_PAYMENT',
  currency CHAR(3) NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL,
  discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  service_fee DECIMAL(12,2) NOT NULL DEFAULT 0,
  total_amount DECIMAL(12,2) NOT NULL,
  reservation_expires_at DATETIME(6) NULL,
  confirmed_at DATETIME(6) NULL,
  cancelled_at DATETIME(6) NULL,
  cancellation_reason VARCHAR(1000) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_bookings_code (booking_code),
  KEY idx_bookings_user_created (user_id, created_at),
  KEY idx_bookings_event_status (event_id, status),
  KEY idx_bookings_pending_expiry (status, reservation_expires_at),
  CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT fk_bookings_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE RESTRICT,
  CONSTRAINT fk_bookings_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE SET NULL,
  CONSTRAINT chk_bookings_amounts CHECK (subtotal >= 0 AND discount_amount >= 0 AND tax_amount >= 0 AND service_fee >= 0 AND total_amount >= 0),
  CONSTRAINT chk_bookings_total CHECK (total_amount = subtotal - discount_amount + tax_amount + service_fee)
) ENGINE=InnoDB;

CREATE TABLE booking_items (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  booking_id BIGINT UNSIGNED NOT NULL,
  ticket_type_id BIGINT UNSIGNED NOT NULL,
  quantity SMALLINT UNSIGNED NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  line_total DECIMAL(12,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_booking_items_ticket_type (booking_id, ticket_type_id),
  KEY idx_booking_items_ticket_type (ticket_type_id),
  CONSTRAINT fk_booking_items_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE RESTRICT,
  CONSTRAINT fk_booking_items_ticket_type FOREIGN KEY (ticket_type_id) REFERENCES ticket_types (id) ON DELETE RESTRICT,
  CONSTRAINT chk_booking_items_values CHECK (quantity > 0 AND unit_price >= 0 AND line_total = quantity * unit_price)
) ENGINE=InnoDB;

CREATE TABLE payments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  booking_id BIGINT UNSIGNED NOT NULL,
  provider ENUM('RAZORPAY') NOT NULL DEFAULT 'RAZORPAY',
  provider_order_id VARCHAR(128) NULL,
  provider_payment_id VARCHAR(128) NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  status ENUM('CREATED','AUTHORIZED','CAPTURED','FAILED','CANCELLED') NOT NULL DEFAULT 'CREATED',
  amount DECIMAL(12,2) NOT NULL,
  currency CHAR(3) NOT NULL,
  failure_code VARCHAR(100) NULL,
  failure_message VARCHAR(500) NULL,
  paid_at DATETIME(6) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payments_idempotency (idempotency_key),
  UNIQUE KEY uk_payments_provider_order (provider, provider_order_id),
  UNIQUE KEY uk_payments_provider_payment (provider, provider_payment_id),
  KEY idx_payments_booking_status (booking_id, status),
  CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE RESTRICT,
  CONSTRAINT chk_payments_amount CHECK (amount > 0)
) ENGINE=InnoDB;

CREATE TABLE refunds (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  payment_id BIGINT UNSIGNED NOT NULL,
  requested_by BIGINT UNSIGNED NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  reason VARCHAR(1000) NOT NULL,
  status ENUM('REQUESTED','APPROVED','REJECTED','PROCESSING','SUCCEEDED','FAILED') NOT NULL DEFAULT 'REQUESTED',
  provider_refund_id VARCHAR(128) NULL,
  reviewed_by BIGINT UNSIGNED NULL,
  reviewed_at DATETIME(6) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_refunds_provider_id (provider_refund_id),
  KEY idx_refunds_status_created (status, created_at),
  KEY idx_refunds_payment (payment_id, status),
  CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE RESTRICT,
  CONSTRAINT fk_refunds_requester FOREIGN KEY (requested_by) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT fk_refunds_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (id) ON DELETE SET NULL,
  CONSTRAINT chk_refunds_amount CHECK (amount > 0)
) ENGINE=InnoDB;

CREATE TABLE coupon_usage (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  coupon_id BIGINT UNSIGNED NOT NULL,
  booking_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  discount_amount DECIMAL(12,2) NOT NULL,
  used_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_coupon_usage_booking (booking_id),
  KEY idx_coupon_usage_user (coupon_id, user_id),
  CONSTRAINT fk_coupon_usage_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE RESTRICT,
  CONSTRAINT fk_coupon_usage_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE RESTRICT,
  CONSTRAINT fk_coupon_usage_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT chk_coupon_usage_discount CHECK (discount_amount >= 0)
) ENGINE=InnoDB;

CREATE TABLE tickets (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  booking_item_id BIGINT UNSIGNED NOT NULL,
  owner_user_id BIGINT UNSIGNED NOT NULL,
  ticket_code CHAR(26) NOT NULL,
  qr_token_hash BINARY(32) NOT NULL,
  status ENUM('VALID','USED','CANCELLED','REFUNDED') NOT NULL DEFAULT 'VALID',
  issued_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  used_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tickets_code (ticket_code),
  UNIQUE KEY uk_tickets_qr_hash (qr_token_hash),
  KEY idx_tickets_owner_status (owner_user_id, status),
  KEY idx_tickets_booking_item (booking_item_id),
  CONSTRAINT fk_tickets_booking_item FOREIGN KEY (booking_item_id) REFERENCES booking_items (id) ON DELETE RESTRICT,
  CONSTRAINT fk_tickets_owner FOREIGN KEY (owner_user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE event_attendees (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT UNSIGNED NOT NULL,
  attendee_name VARCHAR(200) NOT NULL,
  attendee_email VARCHAR(254) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_event_attendees_ticket (ticket_id),
  CONSTRAINT fk_event_attendees_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE check_ins (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT UNSIGNED NOT NULL,
  checked_in_by BIGINT UNSIGNED NOT NULL,
  checked_in_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  entry_gate VARCHAR(100) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_check_ins_ticket (ticket_id),
  KEY idx_check_ins_checked_in_at (checked_in_at),
  CONSTRAINT fk_check_ins_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE RESTRICT,
  CONSTRAINT fk_check_ins_user FOREIGN KEY (checked_in_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE reviews (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  event_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  rating TINYINT UNSIGNED NOT NULL,
  comment VARCHAR(3000) NULL,
  status ENUM('PUBLISHED','HIDDEN','REPORTED') NOT NULL DEFAULT 'PUBLISHED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reviews_event_user (event_id, user_id),
  KEY idx_reviews_event_status (event_id, status, created_at),
  CONSTRAINT fk_reviews_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE RESTRICT,
  CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE wishlists (
  user_id BIGINT UNSIGNED NOT NULL,
  event_id BIGINT UNSIGNED NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, event_id),
  KEY idx_wishlists_event (event_id),
  CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_wishlists_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE notifications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  type VARCHAR(80) NOT NULL,
  title VARCHAR(200) NOT NULL,
  message VARCHAR(2000) NOT NULL,
  data JSON NULL,
  read_at DATETIME(6) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notifications_user_read_created (user_id, read_at, created_at),
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE waitlists (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  ticket_type_id BIGINT UNSIGNED NOT NULL,
  status ENUM('WAITING','OFFERED','BOOKED','CANCELLED','EXPIRED') NOT NULL DEFAULT 'WAITING',
  joined_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  offered_until DATETIME(6) NULL,
  notified_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_waitlists_user_ticket_type (user_id, ticket_type_id),
  KEY idx_waitlists_queue (ticket_type_id, status, joined_at),
  CONSTRAINT fk_waitlists_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT fk_waitlists_ticket_type FOREIGN KEY (ticket_type_id) REFERENCES ticket_types (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  actor_user_id BIGINT UNSIGNED NULL,
  action VARCHAR(120) NOT NULL,
  resource_type VARCHAR(100) NOT NULL,
  resource_id VARCHAR(100) NULL,
  ip_address VARBINARY(16) NULL,
  user_agent VARCHAR(512) NULL,
  details JSON NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_audit_logs_actor_created (actor_user_id, created_at),
  KEY idx_audit_logs_resource (resource_type, resource_id, created_at),
  CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Reference data only. Create the first administrator through a controlled,
-- documented deployment/bootstrap procedure with a BCrypt password hash.
INSERT INTO roles (name, description) VALUES
  ('CUSTOMER', 'Can discover events and manage personal bookings and tickets'),
  ('ORGANIZER', 'Can manage approved organizer profile and owned events'),
  ('ADMIN', 'Can administer platform users, events, bookings, and reports');

INSERT INTO event_categories (name, slug) VALUES
  ('Music', 'music'),
  ('Technology', 'technology'),
  ('Business', 'business'),
  ('Arts & Culture', 'arts-culture'),
  ('Food & Drink', 'food-drink'),
  ('Sports', 'sports'),
  ('Community', 'community');
