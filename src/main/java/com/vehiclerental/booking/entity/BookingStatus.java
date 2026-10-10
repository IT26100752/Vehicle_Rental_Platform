package com.vehiclerental.booking.entity;

public enum BookingStatus {
    ACTIVE,      // vehicle is currently with the customer
    COMPLETED,   // returned and settled
    CANCELLED;

    public String label() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public String badge() {
        return switch (this) {
            case ACTIVE    -> "primary";
            case COMPLETED -> "success";
            case CANCELLED -> "danger";
        };
    }
}
