package com.vehiclerental.booking.entity;

public enum BookingStatus {
    PENDING_PAYMENT("Pending payment", "warn"),
    AWAITING_VERIFICATION("Payment under review", "warn"),
    CONFIRMED("Confirmed", "info"),
    ACTIVE("Active", "ok"),
    FINE_DUE("Late fine due", "danger"),
    COMPLETED("Completed", "done"),
    CANCELLED("Cancelled", "muted");

    private final String label;
    private final String css;

    BookingStatus(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() {
        return label;
    }

    /** Badge colour class used by the templates. */
    public String getCss() {
        return css;
    }

    /** Still part of the customer's "Active Orders". */
    public boolean isOpen() {
        return this != COMPLETED && this != CANCELLED;
    }
}
