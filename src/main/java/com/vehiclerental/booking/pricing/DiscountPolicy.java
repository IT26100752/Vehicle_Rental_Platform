package com.vehiclerental.booking.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * ABSTRACTION - TEMPLATE METHOD pattern.
 * {@link #discountFor} is the fixed algorithm (check, then percentage); subclasses only decide WHEN the discount
 * applies and HOW MUCH it is.
 */
public abstract class DiscountPolicy {

    public final BigDecimal discountFor(BigDecimal amount, long days, LocalDate pickup) {
        if (!isApplicable(days, pickup)) {
            return BigDecimal.ZERO.setScale(2);
        }
        return amount.multiply(rate()).setScale(2, RoundingMode.HALF_UP);
    }

    protected abstract boolean isApplicable(long days, LocalDate pickup);

    /** Fraction, e.g. 0.05 for 5%. */
    protected abstract BigDecimal rate();

    public abstract String describe();
}
