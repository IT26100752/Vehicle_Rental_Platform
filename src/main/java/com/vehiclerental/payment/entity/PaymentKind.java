package com.vehiclerental.payment.entity;

public enum PaymentKind {
    RENTAL("Rental"),
    FINE("Late fine");

    private final String label;

    PaymentKind(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
