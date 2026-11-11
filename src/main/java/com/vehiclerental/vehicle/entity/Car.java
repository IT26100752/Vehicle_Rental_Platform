package com.vehiclerental.vehicle.entity;

/** Passenger car. */
public class Car extends Vehicle {

    private int doors = 4;
    private int bootLitres = 400;

    @Override
    public VehicleType getType() {
        return VehicleType.CAR;
    }

    @Override
    public String getFeatureSummary() {
        return "Comfortable everyday ride - A/C, easy parking and great for city and highway trips";
    }

    @Override
    public String getCapacityInfo() {
        return doors + " doors, " + bootLitres + " L boot";
    }

    public int getDoors() {
        return doors;
    }

    public void setDoors(int doors) {
        this.doors = Math.max(doors, 2);
    }

    public int getBootLitres() {
        return bootLitres;
    }

    public void setBootLitres(int bootLitres) {
        this.bootLitres = Math.max(bootLitres, 0);
    }
}
