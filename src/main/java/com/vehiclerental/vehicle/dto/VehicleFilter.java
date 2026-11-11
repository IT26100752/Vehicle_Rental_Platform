package com.vehiclerental.vehicle.dto;

import com.vehiclerental.vehicle.entity.FuelType;
import com.vehiclerental.vehicle.entity.VehicleType;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Search / filter options on the vehicle list. Every field is optional. */
public class VehicleFilter {

    private String keyword;
    private VehicleType type;
    private FuelType fuelType;
    private Integer minSeats;
    private BigDecimal maxPrice;
    /** When both dates are given, only vehicles free for that whole period are listed. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate pickupDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;
    /** price_asc | price_desc | seats | newest */
    private String sort = "newest";

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }
    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }
    public Integer getMinSeats() { return minSeats; }
    public void setMinSeats(Integer minSeats) { this.minSeats = minSeats; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    /** "?pickupDate=..&returnDate=.." when both dates are chosen, else empty. Appended to vehicle links. */
    public String getDateQuery() {
        return pickupDate != null && returnDate != null
                ? "?pickupDate=" + pickupDate + "&returnDate=" + returnDate : "";
    }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
}
