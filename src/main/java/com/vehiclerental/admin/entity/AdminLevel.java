package com.vehiclerental.admin.entity;

/** The two kinds of staff account. Only a MANAGER may add or remove other administrators. */
public enum AdminLevel {
    MANAGER("Manager admin"),
    ADMIN("Admin");

    private final String label;

    AdminLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
