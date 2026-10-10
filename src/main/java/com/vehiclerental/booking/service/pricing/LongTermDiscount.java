package com.vehiclerental.booking.service.pricing;

import com.vehiclerental.booking.entity.Booking;

/** Extra 5% off for weekly (7+ day) rentals. */
public class LongTermDiscount extends DiscountPolicy {

    @Override
    protected boolean isApplicable(Booking booking) {
        return booking.getRentalDays() >= 7;
    }

    @Override
    protected double discountRate() {
        return 0.05;
    }

    @Override
    public String describe() {
        return "Long-term discount: 5% off for 7+ days";
    }
}
