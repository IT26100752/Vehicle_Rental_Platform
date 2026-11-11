package com.vehiclerental.booking.dto;

/** "Most rented vehicles" row on the admin dashboard. */
public class VehicleRentalCount {

    private final String vehicleModel;
    private final long rentals;
    private final int percent;

    public VehicleRentalCount(String vehicleModel, long rentals, int percent) {
        this.vehicleModel = vehicleModel;
        this.rentals = rentals;
        this.percent = percent;
    }

    public String getVehicleModel() { return vehicleModel; }
    public long getRentals() { return rentals; }
    public int getPercent() { return percent; }
}
