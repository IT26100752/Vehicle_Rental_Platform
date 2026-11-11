package com.vehiclerental.booking.service;

import java.math.BigDecimal;

/**
 * ABSTRACTION for "how much do we charge when a vehicle comes back late".
 * BookingService only knows this interface, so the rule can change without touching it.
 */
public interface LateFeePolicy {

    /**
     * @param originalRentalAmount the amount of the original booking
     * @param lateDays             whole days after the agreed return date
     * @return the fine to add to the final amount (zero when not late)
     */
    BigDecimal calculateFine(BigDecimal originalRentalAmount, long lateDays);

    /** Human readable rule, shown on the booking pages. */
    String describe();
}
