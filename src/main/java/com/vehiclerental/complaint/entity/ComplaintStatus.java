package com.vehiclerental.complaint.entity;

public enum ComplaintStatus {
    OPEN("Open", "warn"),
    IN_PROGRESS("In progress", "info"),
    RESOLVED("Resolved", "ok"),
    CLOSED("Closed", "muted");

    private final String label;
    private final String css;

    ComplaintStatus(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() { return label; }

    public String getCss() { return css; }

    /** Still needs staff attention. */
    public boolean isActive() { return this == OPEN || this == IN_PROGRESS; }
}
