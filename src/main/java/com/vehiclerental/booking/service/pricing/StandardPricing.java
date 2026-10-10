package com.vehiclerental.booking.service.pricing;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.vehicle.entity.Vehicle;

/**
 * The default rule, exactly what the intro session video shows the
 * JavaScript doing on the booking form:
 *      number of days  x  vehicle price per day
 */
public class StandardPricing implements PricingStrategy {

    @Override
    public double calculate(Vehicle vehicle, Booking booking) {
        return booking.getRentalDays() * vehicle.getPricePerDay();
    }

    @Override
    public String describe() {
        return "Standard rate (days x price per day)";
    }
}
