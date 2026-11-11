package com.vehiclerental.driver.entity;

public enum DriverStatus {
    AVAILABLE("Available", "ok"),
    OFF_DUTY("Off duty", "muted");

    private final String label;
    private final String css;

    DriverStatus(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() { return label; }
    public String getCss() { return css; }
}
