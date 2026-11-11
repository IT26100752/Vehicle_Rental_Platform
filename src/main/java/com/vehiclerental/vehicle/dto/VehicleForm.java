package com.vehiclerental.vehicle.dto;

import com.vehiclerental.vehicle.entity.*;
import com.vehiclerental.vehicle.entity.FuelType;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleStatus;
import com.vehiclerental.vehicle.entity.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Admin add / edit vehicle form. */
public class VehicleForm {

    private Long id;

    @NotNull(message = "Choose a vehicle type")
    private VehicleType type = VehicleType.CAR;

    @Size(max = 40, message = "Brand is too long")
    private String brand;

    @Min(value = 1980, message = "Year must be 1980 or later")
    @Max(value = 2100, message = "Year is not valid")
    private Integer year = java.time.Year.now().getValue();

    private Transmission transmission = Transmission.AUTOMATIC;

    /** Type-specific values: Car doors / Van cargoKg / Bike engineCc / Truck maxLoadKg. */
    @Min(value = 0, message = "Cannot be negative")
    private Integer spec = 4;
    /** Car boot litres. */
    @Min(value = 0, message = "Cannot be negative")
    private Integer boot = 400;
    /** Van air conditioned / Bike helmet provided. */
    private boolean flag = true;
    /** Truck body kind. */
    @Size(max = 30, message = "Body kind is too long")
    private String bodyKind = "Box";

    @NotBlank(message = "Vehicle model is required")
    @Size(max = 60, message = "Model is too long")
    private String model;

    @NotBlank(message = "Registration number is required")
    @Size(max = 20, message = "Registration number is too long")
    private String registrationNo;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "1.00", message = "Price must be greater than zero")
    private BigDecimal pricePerDay;

    @NotNull(message = "Choose a fuel type")
    private FuelType fuelType = FuelType.PETROL;

    @NotNull(message = "Seating capacity is required")
    @Min(value = 1, message = "At least 1 seat")
    @Max(value = 60, message = "That is too many seats")
    private Integer seatingCapacity = 4;

    @NotNull(message = "Choose a status")
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Size(max = 200, message = "Image path is too long")
    private String imageUrl;

    @Size(max = 400, message = "Description must be under 400 characters")
    private String description;

    public static VehicleForm from(Vehicle v) {
        VehicleForm f = new VehicleForm();
        f.id = v.getId();
        f.type = v.getType();
        f.model = v.getModel();
        f.brand = v.getBrand();
        f.year = v.getYear();
        f.transmission = v.getTransmission();
        if (v instanceof Car c) { f.spec = c.getDoors(); f.boot = c.getBootLitres(); }
        else if (v instanceof Van c) { f.spec = c.getCargoKg(); f.flag = c.isAirConditioned(); }
        else if (v instanceof Bike c) { f.spec = c.getEngineCc(); f.flag = c.isHelmetProvided(); }
        else if (v instanceof Truck c) { f.spec = c.getMaxLoadKg(); f.bodyKind = c.getBodyKind(); }
        f.registrationNo = v.getRegistrationNo();
        f.pricePerDay = v.getPricePerDay();
        f.fuelType = v.getFuelType();
        f.seatingCapacity = v.getSeatingCapacity();
        f.status = v.getStatus();
        f.imageUrl = v.getImageUrl();
        f.description = v.getDescription();
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }
    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }
    public Integer getSeatingCapacity() { return seatingCapacity; }
    public void setSeatingCapacity(Integer seatingCapacity) { this.seatingCapacity = seatingCapacity; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Transmission getTransmission() { return transmission; }
    public void setTransmission(Transmission transmission) { this.transmission = transmission; }
    public Integer getSpec() { return spec; }
    public void setSpec(Integer spec) { this.spec = spec; }
    public Integer getBoot() { return boot; }
    public void setBoot(Integer boot) { this.boot = boot; }
    public boolean isFlag() { return flag; }
    public void setFlag(boolean flag) { this.flag = flag; }
    public String getBodyKind() { return bodyKind; }
    public void setBodyKind(String bodyKind) { this.bodyKind = bodyKind; }
}
