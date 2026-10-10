package com.vehiclerental.booking.service.pricing;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.vehicle.entity.Vehicle;

/**
 * Premium customers get 10% off when they rent for 3 days or more.
 * Same method signature as StandardPricing - different behaviour.
 */
public class PremiumPricing implements PricingStrategy {

    private static final int MIN_DAYS_FOR_DISCOUNT = 3;
    private static final double DISCOUNT = 0.10;

    @Override
    public double calculate(Vehicle vehicle, Booking booking) {
        double base = booking.getRentalDays() * vehicle.getPricePerDay();
        if (booking.getRentalDays() >= MIN_DAYS_FOR_DISCOUNT) {
            base -= base * DISCOUNT;
        }
        return base;
    }

    @Override
    public String describe() {
        return "Premium rate (10% off for 3+ days)";
    }
}
