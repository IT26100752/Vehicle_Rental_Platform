-- =====================================================================
-- SE1020 Vehicle Rental Service Management System - MySQL schema
-- Runs automatically on every application start (CREATE ... IF NOT EXISTS).
-- You can also run it by hand:  mysql -u root -p1234 < database/database.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS vrs CHARACTER SET utf8mb4;
USE vrs;

CREATE TABLE IF NOT EXISTS users (
    id               INT PRIMARY KEY,
    type             VARCHAR(20)  NOT NULL,          -- ADMIN | CUSTOMER
    full_name        VARCHAR(120) NOT NULL,
    username         VARCHAR(50)  NOT NULL UNIQUE,
    password_hash    VARCHAR(64)  NOT NULL,          -- SHA-256, never plain text
    email            VARCHAR(120),
    phone            VARCHAR(20),
    nic              VARCHAR(20)  NULL,              -- customer only
    address          VARCHAR(255) NULL,              -- customer only
    premium          BOOLEAN      NULL,              -- customer only
    department       VARCHAR(50)  NULL,              -- admin only
    permission_level INT          NULL               -- admin only
);

CREATE TABLE IF NOT EXISTS vehicles (
    id                  INT PRIMARY KEY,
    vehicle_type        VARCHAR(10)  NOT NULL,       -- CAR | VAN | BIKE | TRUCK
    vehicle_number      VARCHAR(20)  NOT NULL,
    brand               VARCHAR(50)  NOT NULL,
    model               VARCHAR(50)  NOT NULL,
    year                INT          NOT NULL,
    price_per_day       DOUBLE       NOT NULL,
    fuel_type           VARCHAR(10)  NOT NULL,
    transmission        VARCHAR(10)  NOT NULL,
    seats               INT          NOT NULL,
    status              VARCHAR(20)  NOT NULL,       -- AVAILABLE | RENTED | IN_MAINTENANCE
    image_url           VARCHAR(255),
    door_count          INT          NULL,           -- car only
    boot_capacity_liters INT         NULL,           -- car only
    cargo_capacity_kg   DOUBLE       NULL,           -- van only
    has_ac              BOOLEAN      NULL,           -- van only
    engine_cc           INT          NULL,           -- bike only
    helmet_provided     BOOLEAN      NULL,           -- bike only
    max_load_kg         DOUBLE       NULL,           -- truck only
    body_kind           VARCHAR(30)  NULL            -- truck only
);

CREATE TABLE IF NOT EXISTS bookings (
    id              INT PRIMARY KEY,
    user_id         INT          NOT NULL,
    vehicle_id      INT          NOT NULL,
    pickup_date     DATE         NOT NULL,
    return_date     DATE         NOT NULL,
    pickup_location VARCHAR(120) NOT NULL,
    return_location VARCHAR(120) NOT NULL,
    total_amount    DOUBLE       NOT NULL,
    status          VARCHAR(20)  NOT NULL,           -- ACTIVE | COMPLETED | CANCELLED
    returned_date   DATE         NULL,
    fine            DOUBLE       NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS payments (
    id                    INT PRIMARY KEY,
    booking_id            INT          NOT NULL,
    amount                DOUBLE       NOT NULL,
    method                VARCHAR(10)  NOT NULL,     -- CARD | CASH | ONLINE
    payment_date          DATE         NOT NULL,
    status                VARCHAR(20)  NOT NULL,     -- PENDING | COMPLETED | FAILED | REFUNDED
    transaction_reference VARCHAR(30)  NOT NULL
);

CREATE TABLE IF NOT EXISTS reviews (
    id          INT PRIMARY KEY,
    user_id     INT          NOT NULL,
    vehicle_id  INT          NOT NULL,
    rating      INT          NOT NULL,
    comment     TEXT         NOT NULL,
    review_date DATE         NOT NULL,
    status      VARCHAR(20)  NOT NULL                -- PENDING | APPROVED | REJECTED
);
