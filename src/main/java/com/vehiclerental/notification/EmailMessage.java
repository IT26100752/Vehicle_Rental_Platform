package com.vehiclerental.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One email, ready to send: recipient, subject, a plain-text body (always), an optional HTML body
 * and optional attachments. Immutable - use {@link #withAttachment} to get a copy with a file added.
 */
public final class EmailMessage {

    private final String to;
    private final String subject;
    private final String textBody;
    private final String htmlBody;
    private final EmailType type;
    private final List<EmailAttachment> attachments;

    public EmailMessage(String to, String subject, String textBody, String htmlBody, EmailType type,
                        List<EmailAttachment> attachments) {
        this.to = to == null ? "" : to.trim();
        this.subject = subject == null ? "" : subject;
        this.textBody = textBody == null ? "" : textBody;
        this.htmlBody = htmlBody;
        this.type = type == null ? EmailType.GENERAL : type;
        this.attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }

    /** A simple plain-text email. */
    public static EmailMessage text(String to, String subject, String body) {
        return new EmailMessage(to, subject, body, null, EmailType.GENERAL, null);
    }

    public EmailMessage withAttachment(EmailAttachment attachment) {
        List<EmailAttachment> copy = new ArrayList<>(attachments);
        if (attachment != null) {
            copy.add(attachment);
        }
        return new EmailMessage(to, subject, textBody, htmlBody, type, copy);
    }

    public EmailMessage withType(EmailType newType) {
        return new EmailMessage(to, subject, textBody, htmlBody, newType, attachments);
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getTextBody() {
        return textBody;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public boolean hasHtml() {
        return htmlBody != null && !htmlBody.isBlank();
    }

    public EmailType getType() {
        return type;
    }

    public List<EmailAttachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }
}
