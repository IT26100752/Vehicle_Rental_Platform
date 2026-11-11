package com.vehiclerental.booking.pricing;

import com.vehiclerental.vehicle.entity.Vehicle;

import java.math.BigDecimal;

/** Default rule: days x price per day. */
public class StandardPricing implements PricingStrategy {

    @Override
    public BigDecimal calculate(Vehicle vehicle, long days) {
        return vehicle.calculateRent(days);
    }

    @Override
    public String describe() {
        return "Standard rate (days x price per day)";
    }
}
