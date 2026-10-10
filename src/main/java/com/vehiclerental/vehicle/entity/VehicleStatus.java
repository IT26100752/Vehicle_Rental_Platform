package com.vehiclerental.vehicle.entity;

/** The "availability" column of the vehicle table. */
public enum VehicleStatus {
    AVAILABLE,
    RENTED,
    IN_MAINTENANCE;

    public String label() {
        return switch (this) {
            case AVAILABLE      -> "Available";
            case RENTED         -> "Rented Out";
            case IN_MAINTENANCE -> "In Maintenance";
        };
    }

    /** Bootstrap badge colour - keeps the view templates clean. */
    public String badge() {
        return switch (this) {
            case AVAILABLE      -> "success";
            case RENTED         -> "warning";
            case IN_MAINTENANCE -> "secondary";
        };
    }
}
