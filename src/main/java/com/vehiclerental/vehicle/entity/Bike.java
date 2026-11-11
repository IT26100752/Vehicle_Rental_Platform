package com.vehiclerental.vehicle.entity;

/** Motorbike. Always self-drive (a driver cannot be added). */
public class Bike extends Vehicle {

    private int engineCc = 150;
    private boolean helmetProvided = true;

    @Override
    public VehicleType getType() {
        return VehicleType.BIKE;
    }

    @Override
    public String getFeatureSummary() {
        return "Nimble and economical - beat the traffic and reach narrow roads";
    }

    @Override
    public String getCapacityInfo() {
        return engineCc + " cc, helmet " + (helmetProvided ? "included" : "not included");
    }

    public int getEngineCc() {
        return engineCc;
    }

    public void setEngineCc(int engineCc) {
        this.engineCc = Math.max(engineCc, 0);
    }

    public boolean isHelmetProvided() {
        return helmetProvided;
    }

    public void setHelmetProvided(boolean helmetProvided) {
        this.helmetProvided = helmetProvided;
    }
}
