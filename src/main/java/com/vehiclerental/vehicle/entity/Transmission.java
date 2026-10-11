package com.vehiclerental.vehicle.entity;

public enum Transmission {
    AUTOMATIC, MANUAL;

    public String label() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
