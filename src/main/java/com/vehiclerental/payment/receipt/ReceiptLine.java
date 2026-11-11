package com.vehiclerental.payment.receipt;

import java.math.BigDecimal;

/** One itemised row on a receipt, e.g. "Rental 3 days x LKR 8,800" or "Weekend discount" (negative amount). */
public class ReceiptLine {

    private final String label;
    private final String detail;
    private final BigDecimal amount;

    public ReceiptLine(String label, String detail, BigDecimal amount) {
        this.label = label;
        this.detail = detail == null ? "" : detail;
        this.amount = amount == null ? BigDecimal.ZERO : amount;
    }

    public String getLabel() { return label; }
    public String getDetail() { return detail; }
    public BigDecimal getAmount() { return amount; }
    public boolean isDiscount() { return amount.signum() < 0; }
}
