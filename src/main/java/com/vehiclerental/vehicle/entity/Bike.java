package com.vehiclerental.vehicle.entity;

/** INHERITANCE - Bike IS-A Vehicle. */
public class Bike extends Vehicle {

    private int engineCc;
    private boolean helmetProvided;

    public Bike() {
    }

    public Bike(int id, String vehicleNumber, String brand, String model, int year,
                double pricePerDay, FuelType fuelType, Transmission transmission,
                int seats, VehicleStatus status, String imageUrl,
                int engineCc, boolean helmetProvided) {
        super(id, vehicleNumber, brand, model, year, pricePerDay, fuelType,
                transmission, seats, status, imageUrl);
        this.engineCc = engineCc;
        this.helmetProvided = helmetProvided;
    }

    @Override
    public VehicleType getType() { return VehicleType.BIKE; }

    @Override
    public String capacityInfo() {
        return engineCc + " cc, helmet " + (helmetProvided ? "included" : "not included");
    }

    public int getEngineCc() { return engineCc; }
    public void setEngineCc(int engineCc) { this.engineCc = engineCc; }

    public boolean isHelmetProvided() { return helmetProvided; }
    public void setHelmetProvided(boolean helmetProvided) { this.helmetProvided = helmetProvided; }
}
