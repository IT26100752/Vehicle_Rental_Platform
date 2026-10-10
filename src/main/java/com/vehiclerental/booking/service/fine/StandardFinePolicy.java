package com.vehiclerental.booking.service.fine;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.vehicle.entity.Vehicle;

/** Regular customers: half a day-rate per overdue day. */
public class StandardFinePolicy implements FinePolicy {

    @Override
    public double fine(Vehicle vehicle, Booking booking, long overdueDays) {
        return overdueDays * vehicle.getPricePerDay() * 0.5;
    }

    @Override
    public String describe() {
        return "50% of the day rate per overdue day";
    }
}
