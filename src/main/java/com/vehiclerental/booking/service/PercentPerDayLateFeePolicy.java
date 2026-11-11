package com.vehiclerental.booking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Project rule: each extra day costs 10% of the ORIGINAL rental amount. */
@Component
public class PercentPerDayLateFeePolicy implements LateFeePolicy {

    private final BigDecimal percentPerDay;

    public PercentPerDayLateFeePolicy(@Value("${app.late-fee.percent-per-day:10}") BigDecimal percentPerDay) {
        this.percentPerDay = percentPerDay;
    }

    @Override
    public BigDecimal calculateFine(BigDecimal originalRentalAmount, long lateDays) {
        if (lateDays <= 0 || originalRentalAmount == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        return originalRentalAmount
                .multiply(percentPerDay)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(lateDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String describe() {
        return percentPerDay.stripTrailingZeros().toPlainString()
                + "% of the original rental amount for each additional day";
    }
}
