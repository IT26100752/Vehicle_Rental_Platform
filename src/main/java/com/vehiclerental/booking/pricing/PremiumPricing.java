package com.vehiclerental.booking.pricing;

import com.vehiclerental.vehicle.entity.Vehicle;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Premium members get 10% off every rental, and 15% off when they rent for 7 days or more. */
public class PremiumPricing implements PricingStrategy {

    private static final long LONG_RENTAL_DAYS = 7;
    private static final BigDecimal KEEP = new BigDecimal("0.90");
    private static final BigDecimal KEEP_LONG = new BigDecimal("0.85");

    @Override
    public BigDecimal calculate(Vehicle vehicle, long days) {
        BigDecimal base = vehicle.calculateRent(days);
        return base.multiply(days >= LONG_RENTAL_DAYS ? KEEP_LONG : KEEP).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String describe() {
        return "Premium rate (10% off, 15% off for 7+ days)";
    }
}
