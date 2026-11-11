package com.vehiclerental.user.entity;

public enum CustomerTier {
    STANDARD("Standard"),
    PREMIUM("Premium");

    private final String label;

    CustomerTier(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
