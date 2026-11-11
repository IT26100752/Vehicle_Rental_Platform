package com.vehiclerental.booking.pricing;

import java.math.BigDecimal;

/** One itemised row of a price: "Rental (3 days x LKR 8,800)  26,400.00". Discounts are negative. */
public class PriceLine {

    private final String label;
    private final BigDecimal amount;
    /** RENTAL, DISCOUNT, DRIVER or FEE - lets the page colour the row. */
    private final String kind;

    public PriceLine(String label, BigDecimal amount, String kind) {
        this.label = label;
        this.amount = amount;
        this.kind = kind;
    }

    public String getLabel() { return label; }
    public BigDecimal getAmount() { return amount; }
    public String getKind() { return kind; }
}
