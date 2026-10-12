package com.vehiclerental.payment.entity;

/**
 * The intro session says a university project may SIMULATE payments:
 * CARD / CASH / ONLINE.
 */
public enum PaymentMethod {
    CARD, CASH, ONLINE;

    public String label() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
