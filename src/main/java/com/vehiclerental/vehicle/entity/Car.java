package com.vehiclerental.vehicle.entity;

/** INHERITANCE - Car IS-A Vehicle. */
public class Car extends Vehicle {

    private int doorCount;
    private int bootCapacityLiters;

    public Car() {
    }

    public Car(int id, String vehicleNumber, String brand, String model, int year,
               double pricePerDay, FuelType fuelType, Transmission transmission,
               int seats, VehicleStatus status, String imageUrl,
               int doorCount, int bootCapacityLiters) {
        super(id, vehicleNumber, brand, model, year, pricePerDay, fuelType,
                transmission, seats, status, imageUrl);
        this.doorCount = doorCount;
        this.bootCapacityLiters = bootCapacityLiters;
    }

    @Override
    public VehicleType getType() { return VehicleType.CAR; }

    /** POLYMORPHISM - car specific answer. */
    @Override
    public String capacityInfo() {
        return doorCount + " doors, " + bootCapacityLiters + " L boot";
    }

    public int getDoorCount() { return doorCount; }
    public void setDoorCount(int doorCount) { this.doorCount = doorCount; }

    public int getBootCapacityLiters() { return bootCapacityLiters; }
    public void setBootCapacityLiters(int bootCapacityLiters) { this.bootCapacityLiters = bootCapacityLiters; }
}
