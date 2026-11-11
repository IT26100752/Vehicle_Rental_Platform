package com.vehiclerental.driver.entity;

public enum AssignmentStatus {
    ASSIGNED("Assigned - waiting for driver", "warn"),
    ACKNOWLEDGED("Driver confirmed", "ok"),
    CANCELLED("Cancelled", "muted");

    private final String label;
    private final String css;

    AssignmentStatus(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() { return label; }
    public String getCss() { return css; }
}
