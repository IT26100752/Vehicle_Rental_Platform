package com.vehiclerental.booking.service.pricing;

import com.vehiclerental.booking.entity.Booking;

/**
 * ABSTRACTION - TEMPLATE METHOD pattern.
 * --------------------------------------
 * apply() is the fixed algorithm (guard -> discount); HOW MUCH the discount
 * is stays abstract and is provided by subclasses.
 */
public abstract class DiscountPolicy {

    /** Final = cannot be changed by subclasses: the algorithm is fixed here. */
    public final double apply(double total, Booking booking) {
        if (!isApplicable(booking)) {
            return total;
        }
        return total - total * discountRate();
    }

    protected abstract boolean isApplicable(Booking booking);

    protected abstract double discountRate();

    public abstract String describe();
}
