package com.vehiclerental.payment.entity;

public enum PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED;

    public String label() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public String badge() {
        return switch (this) {
            case PENDING   -> "warning";
            case COMPLETED -> "success";
            case FAILED    -> "danger";
            case REFUNDED  -> "secondary";
        };
    }
}
