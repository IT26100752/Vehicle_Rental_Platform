package com.vehiclerental.vehicle.entity;

/** INHERITANCE - Van IS-A Vehicle. */
public class Van extends Vehicle {

    private double cargoCapacityKg;
    private boolean hasAc;

    public Van() {
    }

    public Van(int id, String vehicleNumber, String brand, String model, int year,
               double pricePerDay, FuelType fuelType, Transmission transmission,
               int seats, VehicleStatus status, String imageUrl,
               double cargoCapacityKg, boolean hasAc) {
        super(id, vehicleNumber, brand, model, year, pricePerDay, fuelType,
                transmission, seats, status, imageUrl);
        this.cargoCapacityKg = cargoCapacityKg;
        this.hasAc = hasAc;
    }

    @Override
    public VehicleType getType() { return VehicleType.VAN; }

    @Override
    public String capacityInfo() {
        return cargoCapacityKg + " kg cargo, " + (hasAc ? "with A/C" : "no A/C");
    }

    public double getCargoCapacityKg() { return cargoCapacityKg; }
    public void setCargoCapacityKg(double cargoCapacityKg) { this.cargoCapacityKg = cargoCapacityKg; }

    public boolean isHasAc() { return hasAc; }
    public void setHasAc(boolean hasAc) { this.hasAc = hasAc; }
}
