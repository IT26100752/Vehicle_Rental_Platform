package com.vehiclerental.notification;

/** Category of an in-app notification (drives the icon colour in the bell list). */
public enum NotificationType {
    BOOKING("Booking", "info"),
    PAYMENT("Payment", "ok"),
    DRIVER("Driver", "info"),
    COMPLAINT("Complaint", "danger"),
    REVIEW("Review", "warn"),
    ACCOUNT("Account", "muted"),
    SYSTEM("System", "muted");

    private final String label;
    private final String css;

    NotificationType(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() {
        return label;
    }

    public String getCss() {
        return css;
    }
}
