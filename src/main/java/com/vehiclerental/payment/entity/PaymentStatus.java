package com.vehiclerental.payment.entity;

public enum PaymentStatus {
    PAID("Paid", "ok"),
    PENDING_VERIFICATION("Awaiting verification", "warn"),
    REJECTED("Rejected", "danger"),
    FAILED("Failed", "danger"),
    REFUNDED("Refunded", "muted");

    private final String label;
    private final String css;

    PaymentStatus(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() { return label; }
    public String getCss() { return css; }
}
