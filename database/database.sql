-- =====================================================================
--  Vehicle Rental Management System  -  relational schema (MySQL 8 / MariaDB 10.5+)
--  Every table matches one entity class / data file used by the application:
--    customers -> Customer        admins    -> Admin           locations -> Location
--    vehicles  -> Car/Van/Bike/Truck                           drivers   -> Driver
--    bookings  -> Booking         driver_assignments -> DriverAssignment
--    payments  -> Card/Cash/BankTransfer/WalletPayment         reviews   -> Review
--    complaints / complaint_replies -> Complaint / ComplaintReply
--    notifications -> Notification   email_log -> EmailLog   email_verifications -> EmailVerification
--  NOTE: the running application stores its data in the text files under data/ (file handling).
--  This script is the same model as a relational database, for the documentation and for the viva.
--  Run:  mysql -u root -p < database/database.sql
-- =====================================================================
DROP DATABASE IF EXISTS vehicle_rental;
CREATE DATABASE vehicle_rental CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vehicle_rental;

-- ---------------------------------------------------------------- admins
CREATE TABLE admins (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(40)  NOT NULL,
    full_name     VARCHAR(80)  NOT NULL,
    email         VARCHAR(120),
    phone         VARCHAR(20),
    password_hash VARCHAR(100) NOT NULL,              -- BCrypt hash, never plain text
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_admin_username (username)
) ENGINE=InnoDB;

-- ------------------------------------------------------------- customers
CREATE TABLE customers (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    full_name      VARCHAR(80)  NOT NULL,
    email          VARCHAR(120) NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    address        VARCHAR(160),
    nic            VARCHAR(12),                        -- last rental details, used to pre-fill the form
    sex            ENUM('Male','Female','Other'),
    age            TINYINT UNSIGNED,
    licence_id     VARCHAR(15),
    status         ENUM('ACTIVE','BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN      NOT NULL DEFAULT FALSE,   -- must be TRUE before the customer can sign in
    tier           ENUM('STANDARD','PREMIUM') NOT NULL DEFAULT 'STANDARD',  -- drives pricing + late-fee strategy
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_customer_email (email)
) ENGINE=InnoDB;

-- ------------------------------------------------ email verification codes
-- Only a BCrypt hash of the 6 digit code is kept. A new code retires the older ones (used_at is set).
CREATE TABLE email_verifications (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    customer_id  BIGINT       NOT NULL,
    email        VARCHAR(120) NOT NULL,
    code_hash    VARCHAR(100) NOT NULL,
    expires_at   DATETIME     NOT NULL,
    attempts     INT          NOT NULL DEFAULT 0,
    used_at      DATETIME,
    last_sent_at DATETIME,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_verif_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    KEY idx_verif_customer (customer_id)
) ENGINE=InnoDB;

-- -------------------------------------------------------------- locations
CREATE TABLE locations (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(60) NOT NULL,
    city       VARCHAR(40) NOT NULL,
    address    VARCHAR(120),
    phone      VARCHAR(20),
    active     BOOLEAN     NOT NULL DEFAULT TRUE,
    latitude   DECIMAL(9,6),
    longitude  DECIMAL(9,6),
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_location_name (name)
) ENGINE=InnoDB;

-- -------------------------------------------------------------- vehicles
-- Car, Van, Bike and Truck are subclasses of Vehicle: single-table inheritance with a type column.
-- Columns after "description" belong to one subclass each and are NULL for the others.
CREATE TABLE vehicles (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    type             ENUM('CAR','VAN','BIKE','TRUCK') NOT NULL,
    brand            VARCHAR(40),
    model            VARCHAR(60)   NOT NULL,
    model_year       SMALLINT,
    transmission     ENUM('AUTOMATIC','MANUAL') NOT NULL DEFAULT 'AUTOMATIC',
    registration_no  VARCHAR(20)   NOT NULL,
    price_per_day    DECIMAL(10,2) NOT NULL CHECK (price_per_day > 0),
    fuel_type        ENUM('PETROL','DIESEL','HYBRID','ELECTRIC') NOT NULL,
    seating_capacity TINYINT UNSIGNED NOT NULL CHECK (seating_capacity BETWEEN 1 AND 60),
    status           ENUM('AVAILABLE','RESERVED','RENTED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    image_url        VARCHAR(200),
    description      VARCHAR(400),
    doors            TINYINT,        -- Car
    boot_litres      INT,            -- Car
    cargo_kg         INT,            -- Van
    air_conditioned  BOOLEAN,        -- Van
    engine_cc        INT,            -- Bike
    helmet_provided  BOOLEAN,        -- Bike
    max_load_kg      INT,            -- Truck
    body_kind        VARCHAR(30),    -- Truck
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_vehicle_reg (registration_no),
    KEY idx_vehicle_type_status (type, status)
) ENGINE=InnoDB;

-- --------------------------------------------------------------- drivers
CREATE TABLE drivers (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    full_name  VARCHAR(80)  NOT NULL,
    phone      VARCHAR(20)  NOT NULL,
    email      VARCHAR(120),
    licence_no VARCHAR(15)  NOT NULL,
    languages  VARCHAR(60),
    status     ENUM('AVAILABLE','OFF_DUTY') NOT NULL DEFAULT 'AVAILABLE',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_driver_licence (licence_no)
) ENGINE=InnoDB;

-- -------------------------------------------------------------- bookings
CREATE TABLE bookings (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    customer_id        BIGINT        NOT NULL,
    vehicle_id         BIGINT        NULL,             -- NULL if the vehicle was removed later; history survives
    vehicle_model      VARCHAR(80)   NOT NULL,         -- snapshot for history ("Toyota Axio")
    vehicle_reg_no     VARCHAR(20)   NOT NULL,
    pickup_date        DATE          NOT NULL,
    return_date        DATE          NOT NULL,
    actual_return_date DATE,
    pickup_time        TIME,
    pickup_location_id BIGINT        NULL,
    pickup_location    VARCHAR(100),                   -- snapshot of the location label
    drop_location_id   BIGINT        NULL,
    drop_location      VARCHAR(100),
    pickup_address     VARCHAR(150),                   -- door-to-door address for "with driver" trips
    with_driver        BOOLEAN       NOT NULL DEFAULT FALSE,
    driver_id          BIGINT        NULL,
    driver_name        VARCHAR(80),
    driver_phone       VARCHAR(20),
    renter_name        VARCHAR(80)   NOT NULL,
    nic                VARCHAR(12)   NOT NULL,
    sex                ENUM('Male','Female','Other') NOT NULL,
    age                TINYINT UNSIGNED NOT NULL CHECK (age >= 18),
    licence_id         VARCHAR(15),                    -- optional when with_driver = TRUE
    price_per_day      DECIMAL(10,2) NOT NULL,
    base_amount        DECIMAL(12,2) NOT NULL DEFAULT 0,   -- rental before discounts
    discount_amount    DECIMAL(12,2) NOT NULL DEFAULT 0,   -- premium rate + long-term + weekend discounts
    driver_fee         DECIMAL(12,2) NOT NULL DEFAULT 0,   -- driver fee per day x days
    one_way_fee        DECIMAL(12,2) NOT NULL DEFAULT 0,   -- when drop location differs from pickup
    pricing_note       VARCHAR(200),
    total_amount       DECIMAL(12,2) NOT NULL,             -- base - discount + driver + one-way
    late_days          INT           NOT NULL DEFAULT 0,
    fine_amount        DECIMAL(12,2) NOT NULL DEFAULT 0,   -- per customer tier (standard 10%/day, premium first day free then 5%)
    status             ENUM('PENDING_PAYMENT','AWAITING_VERIFICATION','CONFIRMED','ACTIVE','FINE_DUE','COMPLETED','CANCELLED')
                       NOT NULL DEFAULT 'PENDING_PAYMENT',
    hold_expires_at    DATETIME,                           -- payment window (15 minutes)
    docs_verified      BOOLEAN       NOT NULL DEFAULT FALSE, -- original NIC + licence checked at pickup
    verified_by        VARCHAR(80),
    condition_note     VARCHAR(300),
    created_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_dates CHECK (return_date > pickup_date),
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_vehicle  FOREIGN KEY (vehicle_id)  REFERENCES vehicles(id)  ON DELETE SET NULL,
    CONSTRAINT fk_booking_pickup   FOREIGN KEY (pickup_location_id) REFERENCES locations(id) ON DELETE SET NULL,
    CONSTRAINT fk_booking_drop     FOREIGN KEY (drop_location_id)   REFERENCES locations(id) ON DELETE SET NULL,
    CONSTRAINT fk_booking_driver   FOREIGN KEY (driver_id)  REFERENCES drivers(id)   ON DELETE SET NULL,
    KEY idx_booking_vehicle_dates (vehicle_id, pickup_date, return_date),
    KEY idx_booking_status (status)
) ENGINE=InnoDB;

-- ----------------------------------------------------- driver assignments
-- One row per time a driver is given to a booking. token = the secret in the driver's email link.
CREATE TABLE driver_assignments (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    booking_id      BIGINT      NOT NULL,
    driver_id       BIGINT      NOT NULL,
    pickup_date     DATE        NOT NULL,
    return_date     DATE        NOT NULL,
    status          ENUM('ASSIGNED','ACKNOWLEDGED','CANCELLED') NOT NULL DEFAULT 'ASSIGNED',
    token           VARCHAR(40) NOT NULL,
    notified_at     DATETIME,
    acknowledged_at DATETIME,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_assignment_token (token),
    CONSTRAINT fk_assign_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_assign_driver  FOREIGN KEY (driver_id)  REFERENCES drivers(id)  ON DELETE CASCADE,
    KEY idx_assign_driver_dates (driver_id, pickup_date, return_date)
) ENGINE=InnoDB;

-- -------------------------------------------------------------- payments
-- CardPayment, CashPayment, BankTransferPayment and WalletPayment share one table.
-- Only the LAST FOUR card digits are stored (never the full number, CVV or wallet PIN).
CREATE TABLE payments (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    booking_id     BIGINT        NOT NULL,
    customer_id    BIGINT        NOT NULL,
    method         ENUM('CARD','CASH','BANK','WALLET') NOT NULL,
    kind           ENUM('RENTAL','FINE') NOT NULL DEFAULT 'RENTAL',
    amount         DECIMAL(12,2) NOT NULL CHECK (amount >= 0),
    status         ENUM('PAID','PENDING_VERIFICATION','REJECTED','FAILED','REFUNDED') NOT NULL DEFAULT 'PAID',
    reference      VARCHAR(20)   NOT NULL,
    receipt_no     VARCHAR(20),                        -- RCT-2026-00012, set once the money is in
    paid_at        DATETIME,
    failure_reason VARCHAR(160),                       -- declined / rejected reason
    verified_by    VARCHAR(80),                        -- admin who approved or rejected a bank transfer
    card_holder    VARCHAR(60),                        -- CARD
    card_last4     CHAR(4),                            -- CARD
    card_brand     VARCHAR(20),                        -- CARD
    received_by    VARCHAR(80),                        -- CASH
    bank_name      VARCHAR(50),                        -- BANK
    slip_reference VARCHAR(30),                        -- BANK
    transfer_date  DATE,                               -- BANK
    wallet_provider VARCHAR(20),                       -- WALLET
    wallet_mobile  VARCHAR(20),                        -- WALLET (masked)
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_ref (reference),
    CONSTRAINT fk_payment_booking  FOREIGN KEY (booking_id)  REFERENCES bookings(id)  ON DELETE CASCADE,
    CONSTRAINT fk_payment_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    KEY idx_payment_status (status),
    KEY idx_payment_created (created_at)
) ENGINE=InnoDB;

-- --------------------------------------------------------------- reviews
-- Every rating from 1 to 5 is accepted and published (a bad review is still a review). Admin may reply or hide.
CREATE TABLE reviews (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    booking_id    BIGINT      NOT NULL,
    customer_id   BIGINT      NOT NULL,
    customer_name VARCHAR(80) NOT NULL,
    vehicle_id    BIGINT      NULL,
    vehicle_model VARCHAR(80) NOT NULL,
    rating        TINYINT     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment       VARCHAR(500),
    hidden        BOOLEAN     NOT NULL DEFAULT FALSE,   -- admin moderation
    admin_reply   VARCHAR(400),
    replied_at    DATETIME,
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_review_booking (booking_id),           -- one review per rental
    CONSTRAINT fk_review_booking  FOREIGN KEY (booking_id)  REFERENCES bookings(id)  ON DELETE CASCADE,
    CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_vehicle  FOREIGN KEY (vehicle_id)  REFERENCES vehicles(id)  ON DELETE SET NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------ complaints
CREATE TABLE complaints (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    customer_id     BIGINT       NOT NULL,
    customer_name   VARCHAR(80)  NOT NULL,
    booking_id      BIGINT       NULL,                     -- optional link to the rental the complaint is about
    category        ENUM('VEHICLE_CONDITION','DRIVER','BILLING_PAYMENT','BOOKING','STAFF_SERVICE','OTHER') NOT NULL,
    subject         VARCHAR(120) NOT NULL,
    description     VARCHAR(1000) NOT NULL,
    status          ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
    priority        ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
    resolution_note VARCHAR(500),
    resolved_at     DATETIME,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_complaint_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaint_booking  FOREIGN KEY (booking_id)  REFERENCES bookings(id)  ON DELETE SET NULL,
    KEY idx_complaint_status (status)
) ENGINE=InnoDB;

CREATE TABLE complaint_replies (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    complaint_id BIGINT       NOT NULL,
    author_role  ENUM('CUSTOMER','ADMIN') NOT NULL,
    author_name  VARCHAR(80)  NOT NULL,
    message      VARCHAR(1000) NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_reply_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------- notifications
-- The bell icon. recipient_role = ADMIN means "shared by all administrators" (recipient_id is NULL).
CREATE TABLE notifications (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    recipient_role ENUM('ADMIN','CUSTOMER') NOT NULL,
    recipient_id   BIGINT       NULL,
    type           ENUM('BOOKING','PAYMENT','DRIVER','COMPLAINT','REVIEW','ACCOUNT','SYSTEM') NOT NULL,
    title          VARCHAR(120) NOT NULL,
    message        VARCHAR(400) NOT NULL,
    link           VARCHAR(120),
    is_read        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notification_recipient (recipient_role, recipient_id, is_read)
) ENGINE=InnoDB;

-- ------------------------------------------------------------- email log
-- Every email the system sends (or saves to data/outbox when SMTP is off) is recorded here.
CREATE TABLE email_log (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    to_address       VARCHAR(120) NOT NULL,
    subject          VARCHAR(200) NOT NULL,
    type             ENUM('VERIFICATION','BOOKING','PAYMENT','RECEIPT','DRIVER','COMPLAINT','REVIEW','ADMIN_ALERT','GENERAL') NOT NULL,
    status           ENUM('QUEUED','SENT','SAVED','FAILED') NOT NULL,
    transport        VARCHAR(20),
    error_message    VARCHAR(300),
    attachment_names VARCHAR(200),
    attempts         INT          NOT NULL DEFAULT 1,
    body             TEXT,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_email_status (status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------- views
-- Handy reporting views that mirror the admin dashboard figures.
CREATE VIEW v_vehicle_rating AS
SELECT v.id AS vehicle_id, v.brand, v.model,
       ROUND(AVG(r.rating), 1) AS avg_rating, COUNT(r.id) AS review_count
FROM vehicles v LEFT JOIN reviews r ON r.vehicle_id = v.id AND r.hidden = FALSE
GROUP BY v.id, v.brand, v.model;

CREATE VIEW v_most_rented AS
SELECT vehicle_model, COUNT(*) AS rentals
FROM bookings
WHERE status IN ('ACTIVE','FINE_DUE','COMPLETED')
GROUP BY vehicle_model
ORDER BY rentals DESC;

CREATE VIEW v_daily_revenue AS
SELECT DATE(COALESCE(paid_at, created_at)) AS day, SUM(amount) AS revenue
FROM payments WHERE status = 'PAID'
GROUP BY DATE(COALESCE(paid_at, created_at));

CREATE VIEW v_monthly_revenue AS
SELECT DATE_FORMAT(COALESCE(paid_at, created_at), '%Y-%m') AS month, SUM(amount) AS revenue
FROM payments WHERE status = 'PAID'
GROUP BY DATE_FORMAT(COALESCE(paid_at, created_at), '%Y-%m');

-- The admin "Needs your attention" row
CREATE VIEW v_attention AS
SELECT
  (SELECT COUNT(*) FROM payments   WHERE status = 'PENDING_VERIFICATION')                      AS transfers_to_verify,
  (SELECT COUNT(*) FROM complaints WHERE status IN ('OPEN','IN_PROGRESS'))                      AS open_complaints,
  (SELECT COUNT(*) FROM bookings   WHERE with_driver AND driver_id IS NULL AND status = 'CONFIRMED') AS bookings_without_driver,
  (SELECT COUNT(*) FROM reviews    WHERE rating <= 2 AND admin_reply IS NULL)                   AS low_reviews_unanswered;

-- ---------------------------------------------- availability check (same rule as the Java code)
-- A vehicle is free for [:pickup, :return) when no booking that still holds it overlaps.
-- Boundaries are exclusive, so a car returned on the 10th can be picked up again on the 10th.
--   SELECT COUNT(*) = 0 AS is_available
--   FROM bookings
--   WHERE vehicle_id = :vehicleId
--     AND status IN ('PENDING_PAYMENT','AWAITING_VERIFICATION','CONFIRMED','ACTIVE')
--     AND :pickup < return_date AND pickup_date < :return;

-- ---------------------------------------------- driver availability
--   SELECT d.* FROM drivers d
--   WHERE d.status = 'AVAILABLE'
--     AND NOT EXISTS (SELECT 1 FROM driver_assignments a
--                     WHERE a.driver_id = d.id AND a.status <> 'CANCELLED'
--                       AND a.pickup_date <= :return AND a.return_date >= :pickup);
