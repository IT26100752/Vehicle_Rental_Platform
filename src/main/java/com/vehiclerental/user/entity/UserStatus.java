package com.vehiclerental.user.entity;

public enum UserStatus {
    ACTIVE("Active"),
    BLOCKED("Blocked");

    private final String label;

    UserStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
