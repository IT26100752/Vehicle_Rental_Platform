package com.vehiclerental.vehicle.entity;

/** The "type" column from the video's vehicle table. */
public enum VehicleType {
    CAR, VAN, BIKE, TRUCK;

    public String label() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
