package com.vehiclerental.booking.service.pricing;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.vehicle.entity.Vehicle;

/**
 * POLYMORPHISM (interface based)
 * ------------------------------
 * "The same message (calculate) sent to different objects behaves differently."
 * BookingService picks the implementation at RUNTIME depending on the
 * customer type - classic runtime polymorphism.
 */
public interface PricingStrategy {

    double calculate(Vehicle vehicle, Booking booking);

    String describe();
}
