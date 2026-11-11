package com.vehiclerental.vehicle.entity;

public enum VehicleType {
    CAR("Car"),
    VAN("Van"),
    BIKE("Bike"),
    TRUCK("Truck");

    private final String label;

    VehicleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
