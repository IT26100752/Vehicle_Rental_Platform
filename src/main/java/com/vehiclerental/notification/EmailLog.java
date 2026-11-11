package com.vehiclerental.notification;

import com.vehiclerental.common.BaseEntity;

/** One row of the admin "Email log": who was mailed, what, and whether it worked. */
public class EmailLog extends BaseEntity {

    private String toAddress;
    private String subject;
    private EmailType type = EmailType.GENERAL;
    private EmailStatus status = EmailStatus.QUEUED;
    private String transport;
    private String errorMessage;
    /** Plain-text body, kept so a failed email can be sent again. */
    private String body;
    /** Comma separated attachment names (the files themselves are not stored in the log). */
    private String attachmentNames;
    private int attempts;

    public static EmailLog of(EmailMessage message, String transportName) {
        EmailLog log = new EmailLog();
        log.setToAddress(message.getTo());
        log.setSubject(message.getSubject());
        log.setType(message.getType());
        log.setBody(message.getTextBody());
        log.setTransport(transportName);
        StringBuilder names = new StringBuilder();
        for (EmailAttachment a : message.getAttachments()) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(a.getFilename());
        }
        log.setAttachmentNames(names.toString());
        return log;
    }

    public boolean isFailed() {
        return status == EmailStatus.FAILED;
    }

    public boolean hasAttachments() {
        return attachmentNames != null && !attachmentNames.isBlank();
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public EmailType getType() {
        return type;
    }

    public void setType(EmailType type) {
        this.type = type == null ? EmailType.GENERAL : type;
    }

    public EmailStatus getStatus() {
        return status;
    }

    public void setStatus(EmailStatus status) {
        this.status = status == null ? EmailStatus.QUEUED : status;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAttachmentNames() {
        return attachmentNames;
    }

    public void setAttachmentNames(String attachmentNames) {
        this.attachmentNames = attachmentNames;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
}
