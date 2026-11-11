package com.vehiclerental.booking.pricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** The full, itemised price of a booking. Immutable once built by {@link PricingService}. */
public class PriceQuote {

    private final long days;
    private final List<PriceLine> lines;
    private final BigDecimal baseAmount;
    private final BigDecimal discountAmount;
    private final BigDecimal driverFee;
    private final BigDecimal oneWayFee;
    private final BigDecimal total;
    private final String description;

    public PriceQuote(long days, List<PriceLine> lines, BigDecimal baseAmount, BigDecimal discountAmount,
                      BigDecimal driverFee, BigDecimal oneWayFee, BigDecimal total, String description) {
        this.days = days;
        this.lines = new ArrayList<>(lines);
        this.baseAmount = baseAmount;
        this.discountAmount = discountAmount;
        this.driverFee = driverFee;
        this.oneWayFee = oneWayFee;
        this.total = total;
        this.description = description;
    }

    public long getDays() { return days; }
    public List<PriceLine> getLines() { return lines; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getDriverFee() { return driverFee; }
    public BigDecimal getOneWayFee() { return oneWayFee; }
    public BigDecimal getTotal() { return total; }
    /** Which pricing rule was used, e.g. "Premium rate (10% off for 3+ days)". */
    public String getDescription() { return description; }
}
