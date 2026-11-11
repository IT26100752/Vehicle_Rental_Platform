package com.vehiclerental.vehicle.entity;

/** Light truck for moving goods. */
public class Truck extends Vehicle {

    private int maxLoadKg = 1000;
    private String bodyKind = "box";

    @Override
    public VehicleType getType() {
        return VehicleType.TRUCK;
    }

    @Override
    public String getFeatureSummary() {
        return "Load-carrying workhorse for house moves, deliveries and events";
    }

    @Override
    public String getCapacityInfo() {
        return maxLoadKg + " kg max load, " + bodyKind + " body";
    }

    public int getMaxLoadKg() {
        return maxLoadKg;
    }

    public void setMaxLoadKg(int maxLoadKg) {
        this.maxLoadKg = Math.max(maxLoadKg, 0);
    }

    public String getBodyKind() {
        return bodyKind;
    }

    public void setBodyKind(String bodyKind) {
        this.bodyKind = bodyKind == null || bodyKind.isBlank() ? "box" : bodyKind.trim();
    }
}
