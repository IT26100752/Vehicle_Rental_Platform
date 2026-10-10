package com.vehiclerental.vehicle.entity;

public enum FuelType {
    PETROL, DIESEL, ELECTRIC, HYBRID;

    public String label() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
