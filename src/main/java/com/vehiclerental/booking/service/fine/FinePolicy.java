package com.vehiclerental.booking.service.fine;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.vehicle.entity.Vehicle;

/**
 * POLYMORPHISM again, for late-return fines:
 * "different fine calculation methods for regular and premium users".
 */
public interface FinePolicy {

    /**
     * @param overdueDays how many days past the promised return date
     */
    double fine(Vehicle vehicle, Booking booking, long overdueDays);

    String describe();
}
