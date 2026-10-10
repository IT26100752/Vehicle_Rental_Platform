package com.vehiclerental.booking.service.pricing;

import com.vehiclerental.booking.entity.Booking;

import java.time.DayOfWeek;

/** 2% off when the rental starts on a weekend (Saturday pickup). */
public class WeekendDiscount extends DiscountPolicy {

    @Override
    protected boolean isApplicable(Booking booking) {
        return booking.getPickupDate() != null
                && booking.getPickupDate().getDayOfWeek() == DayOfWeek.SATURDAY;
    }

    @Override
    protected double discountRate() {
        return 0.02;
    }

    @Override
    public String describe() {
        return "Weekend pickup discount: 2% off";
    }
}
