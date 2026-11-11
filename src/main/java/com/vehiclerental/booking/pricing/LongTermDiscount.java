package com.vehiclerental.booking.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 5% off weekly (7+ day) rentals. */
public class LongTermDiscount extends DiscountPolicy {

    @Override
    protected boolean isApplicable(long days, LocalDate pickup) {
        return days >= 7;
    }

    @Override
    protected BigDecimal rate() {
        return new BigDecimal("0.05");
    }

    @Override
    public String describe() {
        return "Long-term discount (5% off 7+ days)";
    }
}
