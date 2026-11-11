package com.vehiclerental.vehicle.dto;

import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleStatus;

/** A vehicle plus the extra things the list / detail pages show (derived status and rating). */
public class VehicleCard {

    private final Vehicle vehicle;
    private final VehicleStatus displayStatus;
    private final double averageRating;
    private final int reviewCount;

    public VehicleCard(Vehicle vehicle, VehicleStatus displayStatus, double averageRating, int reviewCount) {
        this.vehicle = vehicle;
        this.displayStatus = displayStatus;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public Vehicle getVehicle() { return vehicle; }
    public VehicleStatus getDisplayStatus() { return displayStatus; }
    public double getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
    public boolean isBookable() { return displayStatus != VehicleStatus.MAINTENANCE; }
}
