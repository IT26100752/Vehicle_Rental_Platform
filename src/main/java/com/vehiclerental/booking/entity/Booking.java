package com.vehiclerental.booking.entity;

import com.vehiclerental.common.entity.Identifiable;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * BOOKING / RENTAL  (Member 3's entity, field list from the intro session)
 *   bookingId, userId, vehicleId, pickupDate, returnDate,
 *   pickupLocation, returnLocation, totalAmount, status
 * plus returnedDate & fine which the return process fills in.
 */
public class Booking implements Identifiable {

    private int id;                 // bookingId
    private int userId;             // the customer who booked
    private int vehicleId;
    private LocalDate pickupDate;
    private LocalDate returnDate;   // the promised return date
    private String pickupLocation;
    private String returnLocation;
    private double totalAmount;     // calculated by the pricing strategy
    private BookingStatus status;

    private LocalDate returnedDate; // actual return date (null until returned)
    private double fine;            // late fee, 0 when returned on time

    public Booking() {
    }

    public Booking(int id, int userId, int vehicleId, LocalDate pickupDate, LocalDate returnDate,
                   String pickupLocation, String returnLocation, double totalAmount,
                   BookingStatus status) {
        this.id = id;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        this.pickupLocation = pickupLocation;
        this.returnLocation = returnLocation;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    /** Rental length in days, minimum 1 day. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public int getRentalDays() {
        if (pickupDate == null || returnDate == null) return 1;
        long days = ChronoUnit.DAYS.between(pickupDate, returnDate);
        return (int) Math.max(1, days);
    }

    /** Days past the promised return date (0 when on time / not returned yet). */
    public long overdueDaysOn(LocalDate actualReturn) {
        if (returnDate == null || actualReturn == null) return 0;
        return Math.max(0, ChronoUnit.DAYS.between(returnDate, actualReturn));
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isActive() {
        return status == BookingStatus.ACTIVE;
    }

    /* ------------------------- getters / setters ---------------------- */
    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getReturnLocation() { return returnLocation; }
    public void setReturnLocation(String returnLocation) { this.returnLocation = returnLocation; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDate getReturnedDate() { return returnedDate; }
    public void setReturnedDate(LocalDate returnedDate) { this.returnedDate = returnedDate; }

    public double getFine() { return fine; }
    public void setFine(double fine) { this.fine = fine; }

    @Override
    public String toString() {
        return "Booking #" + id + " user=" + userId + " vehicle=" + vehicleId
                + " " + pickupDate + " -> " + returnDate + " [" + status + "]";
    }
}
