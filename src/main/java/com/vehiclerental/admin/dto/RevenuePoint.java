package com.vehiclerental.admin.dto;

import java.math.BigDecimal;

/** One bar of a revenue chart. */
public class RevenuePoint {

    private final String label;
    private final BigDecimal amount;
    private final int percent;

    public RevenuePoint(String label, BigDecimal amount, int percent) {
        this.label = label;
        this.amount = amount;
        this.percent = percent;
    }

    public String getLabel() { return label; }
    public BigDecimal getAmount() { return amount; }
    public int getPercent() { return percent; }
}
