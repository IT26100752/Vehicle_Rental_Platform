package com.vehiclerental.complaint.entity;

public enum ComplaintPriority {
    LOW("Low", "muted"),
    MEDIUM("Medium", "warn"),
    HIGH("High", "danger");

    private final String label;
    private final String css;

    ComplaintPriority(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() { return label; }

    public String getCss() { return css; }
}
