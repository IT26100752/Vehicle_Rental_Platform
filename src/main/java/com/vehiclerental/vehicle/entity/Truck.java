package com.vehiclerental.vehicle.entity;

/** INHERITANCE - Truck IS-A Vehicle. */
public class Truck extends Vehicle {

    private double maxLoadKg;
    private String bodyKind;   // e.g. "box", "flatbed", "tipper"

    public Truck() {
    }

    public Truck(int id, String vehicleNumber, String brand, String model, int year,
                 double pricePerDay, FuelType fuelType, Transmission transmission,
                 int seats, VehicleStatus status, String imageUrl,
                 double maxLoadKg, String bodyKind) {
        super(id, vehicleNumber, brand, model, year, pricePerDay, fuelType,
                transmission, seats, status, imageUrl);
        this.maxLoadKg = maxLoadKg;
        this.bodyKind = bodyKind;
    }

    @Override
    public VehicleType getType() { return VehicleType.TRUCK; }

    @Override
    public String capacityInfo() {
        return maxLoadKg + " kg max load, " + bodyKind + " body";
    }

    public double getMaxLoadKg() { return maxLoadKg; }
    public void setMaxLoadKg(double maxLoadKg) { this.maxLoadKg = maxLoadKg; }

    public String getBodyKind() { return bodyKind; }
    public void setBodyKind(String bodyKind) { this.bodyKind = bodyKind; }
}
