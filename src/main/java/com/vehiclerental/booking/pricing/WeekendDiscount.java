package com.vehiclerental.booking.pricing;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

/** 2% off when the rental starts on a Saturday. */
public class WeekendDiscount extends DiscountPolicy {

    @Override
    protected boolean isApplicable(long days, LocalDate pickup) {
        return pickup != null && pickup.getDayOfWeek() == DayOfWeek.SATURDAY;
    }

    @Override
    protected BigDecimal rate() {
        return new BigDecimal("0.02");
    }

    @Override
    public String describe() {
        return "Weekend pickup discount (2% off)";
    }
}
