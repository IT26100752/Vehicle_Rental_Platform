package com.vehiclerental.vehicle.entity;

import com.vehiclerental.common.BaseEntity;

import java.math.BigDecimal;

/**
 * Abstract vehicle. Car, Van, Bike and Truck extend it.
 *
 * ABSTRACTION  : {@link #getType()}, {@link #getFeatureSummary()} and {@link #getCapacityInfo()} are abstract.
 * POLYMORPHISM : code that holds a Vehicle calls the same methods and gets
 *                car-, van-, bike- or truck-specific answers.
 * ENCAPSULATION: private fields, validated setters.
 */
public abstract class Vehicle extends BaseEntity {

    private String brand = "";
    private String model;
    private int year;
    private Transmission transmission = Transmission.AUTOMATIC;
    private String registrationNo;
    private BigDecimal pricePerDay = BigDecimal.ZERO;
    private FuelType fuelType = FuelType.PETROL;
    private int seatingCapacity = 4;
    /** Stored status. RESERVED is normally derived from bookings, see BookingService.displayStatus. */
    private VehicleStatus status = VehicleStatus.AVAILABLE;
    private String imageUrl;
    private String description;

    public abstract VehicleType getType();

    /** One line describing what this kind of vehicle is good for. */
    public abstract String getFeatureSummary();

    /** Type-specific capacity, e.g. "450 L boot, 4 doors" or "3500 kg max load, box body". */
    public abstract String getCapacityInfo();

    /** Whether the vehicle can be rented with a driver (everything except a motorbike). */
    public boolean isDriverAvailable() {
        return getType() != VehicleType.BIKE;
    }

    /** "Toyota Axio" - brand and model together; used wherever a customer sees the vehicle name. */
    public String getFullName() {
        String b = brand == null ? "" : brand.trim();
        String m = model == null ? "" : model.trim();
        return (b.isEmpty() || m.toLowerCase().startsWith(b.toLowerCase())) ? m : b + " " + m;
    }

    /** "2019 Toyota Axio (CAD-7783)". */
    public String getDisplayName() {
        return (year > 0 ? year + " " : "") + getFullName() + " (" + registrationNo + ")";
    }

    /** Rental amount for the given number of days (subclasses may override with their own pricing). */
    public BigDecimal calculateRent(long days) {
        return pricePerDay.multiply(BigDecimal.valueOf(days));
    }

    public boolean isUnderMaintenance() {
        return status == VehicleStatus.MAINTENANCE;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand == null ? "" : brand.trim();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = Math.max(year, 0);
    }

    public Transmission getTransmission() {
        return transmission;
    }

    public void setTransmission(Transmission transmission) {
        this.transmission = transmission == null ? Transmission.AUTOMATIC : transmission;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model == null ? null : model.trim();
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo == null ? null : registrationNo.trim().toUpperCase();
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        if (pricePerDay == null || pricePerDay.signum() < 0) {
            throw new IllegalArgumentException("Price per day cannot be negative");
        }
        this.pricePerDay = pricePerDay;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType == null ? FuelType.PETROL : fuelType;
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(int seatingCapacity) {
        if (seatingCapacity < 1) {
            throw new IllegalArgumentException("Seating capacity must be at least 1");
        }
        this.seatingCapacity = seatingCapacity;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status == null ? VehicleStatus.AVAILABLE : status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}
