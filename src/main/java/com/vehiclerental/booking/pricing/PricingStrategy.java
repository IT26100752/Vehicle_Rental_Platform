package com.vehiclerental.booking.pricing;

import com.vehiclerental.vehicle.entity.Vehicle;

import java.math.BigDecimal;

/**
 * POLYMORPHISM (interface based): the same message "price this rental" gives a different answer for a
 * standard and a premium customer. {@link PricingService} picks the strategy at run time from the customer's tier.
 */
public interface PricingStrategy {

    /** Rental amount before extra discounts, fees and the driver. */
    BigDecimal calculate(Vehicle vehicle, long days);

    String describe();
}
