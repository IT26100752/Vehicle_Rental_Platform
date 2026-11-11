package com.vehiclerental.complaint.entity;

public enum ComplaintCategory {
    VEHICLE_CONDITION("Vehicle condition"),
    DRIVER("Driver behaviour"),
    BILLING_PAYMENT("Billing / payment"),
    BOOKING("Booking"),
    STAFF_SERVICE("Staff / service"),
    OTHER("Other");

    private final String label;

    ComplaintCategory(String label) { this.label = label; }

    public String getLabel() { return label; }
}
