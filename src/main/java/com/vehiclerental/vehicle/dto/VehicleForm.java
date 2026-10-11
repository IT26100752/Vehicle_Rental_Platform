package com.vehiclerental.vehicle.dto;

import com.vehiclerental.vehicle.entity.*;

/**
 * Data carried by the add / edit vehicle form.
 * toVehicle() is the factory that picks the concrete subclass
 * (Car / Van / Bike / Truck) at RUNTIME from vehicleType - polymorphism.
 */
public class VehicleForm {

    private VehicleType vehicleType;
    private String vehicleNumber;
    private String brand;
    private String model;
    private int year;
    private double pricePerDay;
    private FuelType fuelType;
    private Transmission transmission;
    private int seats;
    private VehicleStatus status;
    private String imageUrl;
    private Integer doorCount;
    private Integer bootCapacityLiters;
    private Double cargoCapacityKg;
    private Boolean hasAc;
    private Integer engineCc;
    private Boolean helmetProvided;
    private Double maxLoadKg;
    private String bodyKind;

    public Vehicle toVehicle(int id, String image) {
        return switch (vehicleType) {
            case CAR -> new Car(id, vehicleNumber, brand, model, year, pricePerDay, fuelType,
                    transmission, seats, status, image, orZero(doorCount), orZero(bootCapacityLiters));
            case VAN -> new Van(id, vehicleNumber, brand, model, year, pricePerDay, fuelType,
                    transmission, seats, status, image, orZeroD(cargoCapacityKg), Boolean.TRUE.equals(hasAc));
            case BIKE -> new Bike(id, vehicleNumber, brand, model, year, pricePerDay, fuelType,
                    transmission, seats, status, image, orZero(engineCc), Boolean.TRUE.equals(helmetProvided));
            case TRUCK -> new Truck(id, vehicleNumber, brand, model, year, pricePerDay, fuelType,
                    transmission, seats, status, image, orZeroD(maxLoadKg), bodyKind == null ? "box" : bodyKind);
        };
    }

    private static int orZero(Integer v) { return v == null ? 0 : v; }
    private static double orZeroD(Double v) { return v == null ? 0 : v; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
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
    public Integer getDoorCount() { return doorCount; }
    public void setDoorCount(Integer doorCount) { this.doorCount = doorCount; }
    public Integer getBootCapacityLiters() { return bootCapacityLiters; }
    public void setBootCapacityLiters(Integer bootCapacityLiters) { this.bootCapacityLiters = bootCapacityLiters; }
    public Double getCargoCapacityKg() { return cargoCapacityKg; }
    public void setCargoCapacityKg(Double cargoCapacityKg) { this.cargoCapacityKg = cargoCapacityKg; }
    public Boolean getHasAc() { return hasAc; }
    public void setHasAc(Boolean hasAc) { this.hasAc = hasAc; }
    public Integer getEngineCc() { return engineCc; }
    public void setEngineCc(Integer engineCc) { this.engineCc = engineCc; }
    public Boolean getHelmetProvided() { return helmetProvided; }
    public void setHelmetProvided(Boolean helmetProvided) { this.helmetProvided = helmetProvided; }
    public Double getMaxLoadKg() { return maxLoadKg; }
    public void setMaxLoadKg(Double maxLoadKg) { this.maxLoadKg = maxLoadKg; }
    public String getBodyKind() { return bodyKind; }
    public void setBodyKind(String bodyKind) { this.bodyKind = bodyKind; }
}
