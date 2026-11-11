package com.vehiclerental.vehicle.entity;

/** Passenger / cargo van for groups and long trips. */
public class Van extends Vehicle {

    private int cargoKg = 700;
    private boolean airConditioned = true;

    @Override
    public VehicleType getType() {
        return VehicleType.VAN;
    }

    @Override
    public String getFeatureSummary() {
        return "Spacious group transport - extra luggage room, ideal for families, tours and events";
    }

    @Override
    public String getCapacityInfo() {
        return cargoKg + " kg cargo, " + (airConditioned ? "air conditioned" : "no A/C");
    }

    public int getCargoKg() {
        return cargoKg;
    }

    public void setCargoKg(int cargoKg) {
        this.cargoKg = Math.max(cargoKg, 0);
    }

    public boolean isAirConditioned() {
        return airConditioned;
    }

    public void setAirConditioned(boolean airConditioned) {
        this.airConditioned = airConditioned;
    }
}
