package com.vehiclerental.notification;

/** Life-cycle of one outgoing email, as shown in the admin email log. */
public enum EmailStatus {
    QUEUED("Queued", "warn"),
    SENT("Sent", "ok"),
    SAVED("Saved to outbox", "info"),
    FAILED("Failed", "danger");

    private final String label;
    private final String css;

    EmailStatus(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() {
        return label;
    }

    /** Badge colour class used by the templates. */
    public String getCss() {
        return css;
    }
}
