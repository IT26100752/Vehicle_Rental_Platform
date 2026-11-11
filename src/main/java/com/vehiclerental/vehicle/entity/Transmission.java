package com.vehiclerental.vehicle.entity;

public enum Transmission {
    AUTOMATIC("Automatic"),
    MANUAL("Manual");

    private final String label;

    Transmission(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
