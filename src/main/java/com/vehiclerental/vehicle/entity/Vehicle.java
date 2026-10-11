package com.vehiclerental.vehicle.entity;

import com.vehiclerental.common.entity.Identifiable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * VEHICLE (abstract base class of the vehicle hierarchy)
 * ------------------------------------------------------
 * Field list follows the module plan from the intro session:
 *   vehicleId, vehicleNumber, brand, model, type, year, pricePerDay,
 *   fuelType, transmission, seats, availability, image
 *
 * OOP concepts
 *  - ENCAPSULATION  : private fields + getters/setters.
 *  - INHERITANCE    : Car, Van, Bike and Truck extend Vehicle.
 *  - POLYMORPHISM   : capacityInfo() is abstract; every subclass answers
 *                     differently ("450 L boot", "1200 kg cargo", ...).
 *  - ABSTRACTION    : the rest of the system only ever talks to "Vehicle";
 *                     it never needs to know the concrete subclass.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "vehicleType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Car.class,   name = "CAR"),
        @JsonSubTypes.Type(value = Van.class,   name = "VAN"),
        @JsonSubTypes.Type(value = Bike.class,  name = "BIKE"),
        @JsonSubTypes.Type(value = Truck.class, name = "TRUCK")
})
public abstract class Vehicle implements Identifiable {

    private int id;                       // vehicleId
    private String vehicleNumber;         // e.g. "CBA-1234"
    private String brand;                 // e.g. "Toyota"
    private String model;                 // e.g. "Axio"
    private int year;                     // manufacture year
    private double pricePerDay;           // LKR per day
    private FuelType fuelType;
    private Transmission transmission;
    private int seats;
    private VehicleStatus status;         // availability
    private String imageUrl;              // image path / url

    protected Vehicle() {
    }

    protected Vehicle(int id, String vehicleNumber, String brand, String model, int year,
                      double pricePerDay, FuelType fuelType, Transmission transmission,
                      int seats, VehicleStatus status, String imageUrl) {
        this.id = id;
        this.vehicleNumber = vehicleNumber;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.pricePerDay = pricePerDay;
        this.fuelType = fuelType;
        this.transmission = transmission;
        this.seats = seats;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    /**
     * The "type" column in the UI comes from this getter, but the storage
     * layer already keeps the concrete class (JSON type-id / vehicle_type
     * column), so Jackson ignores this getter.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public abstract VehicleType getType();

    /** POLYMORPHISM - each subclass describes its carrying capacity its own way. */
    public abstract String capacityInfo();

    public String getDisplayName() {
        return year + " " + brand + " " + model + " (" + vehicleNumber + ")";
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isAvailable() {
        return status == VehicleStatus.AVAILABLE;
    }

    /* ------------------------- getters / setters ---------------------- */
    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }

    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

    public Transmission getTransmission() { return transmission; }
    public void setTransmission(Transmission transmission) { this.transmission = transmission; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }

    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return getType().label() + " #" + id + " " + getDisplayName();
    }
}
