package com.vehiclerental.booking.service.fine;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.vehicle.entity.Vehicle;

/** Premium customers: first overdue day is free, then 25% of the day rate. */
public class PremiumFinePolicy implements FinePolicy {

    @Override
    public double fine(Vehicle vehicle, Booking booking, long overdueDays) {
        long chargeable = Math.max(0, overdueDays - 1);
        return chargeable * vehicle.getPricePerDay() * 0.25;
    }

    @Override
    public String describe() {
        return "First day free, then 25% of the day rate per overdue day";
    }
}
