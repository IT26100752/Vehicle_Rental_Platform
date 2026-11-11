package com.vehiclerental.notification;

/** What an email is about. Used by the admin email log to filter and label messages. */
public enum EmailType {
    VERIFICATION("Account verification"),
    BOOKING("Booking"),
    PAYMENT("Payment"),
    RECEIPT("Receipt"),
    DRIVER("Driver"),
    COMPLAINT("Complaint"),
    REVIEW("Review"),
    ADMIN_ALERT("Admin alert"),
    GENERAL("General");

    private final String label;

    EmailType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
