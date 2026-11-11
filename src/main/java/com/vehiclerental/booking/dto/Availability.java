package com.vehiclerental.booking.dto;

import com.vehiclerental.booking.pricing.PriceLine;

import java.math.BigDecimal;
import java.util.List;

/** Result of an availability check, also returned as JSON to the booking form (live price preview). */
public class Availability {

    private final boolean available;
    private final String message;
    private final long days;
    private final BigDecimal total;
    private final List<PriceLine> lines;

    public Availability(boolean available, String message, long days, BigDecimal total, List<PriceLine> lines) {
        this.available = available;
        this.message = message;
        this.days = days;
        this.total = total;
        this.lines = lines == null ? List.of() : lines;
    }

    public static Availability no(String message) {
        return new Availability(false, message, 0, BigDecimal.ZERO, List.of());
    }

    public boolean isAvailable() { return available; }
    public String getMessage() { return message; }
    public long getDays() { return days; }
    public BigDecimal getTotal() { return total; }
    /** Itemised price rows: [{label, amount, kind}]. */
    public List<PriceLine> getLines() { return lines; }
}
