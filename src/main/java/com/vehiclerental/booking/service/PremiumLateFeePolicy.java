package com.vehiclerental.booking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Premium members: the first late day is free, then 5% of the original rental per extra day. */
public class PremiumLateFeePolicy implements LateFeePolicy {

    private static final BigDecimal PERCENT = new BigDecimal("5");

    @Override
    public BigDecimal calculateFine(BigDecimal originalRentalAmount, long lateDays) {
        long chargeable = lateDays - 1;
        if (chargeable <= 0 || originalRentalAmount == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        return originalRentalAmount.multiply(PERCENT).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(chargeable)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String describe() {
        return "first late day free, then 5% of the original rental amount per extra day";
    }
}
