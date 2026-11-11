package com.vehiclerental.notification;

/**
 * ABSTRACTION for sending mail, so no other package cares how a message really leaves the system.
 * The only implementation is {@link EmailDispatcher}; it delegates to a {@link MailTransport}
 * (outbox files or real SMTP), records every message in the email log and never throws.
 */
public interface EmailService {

    /** Sends (or queues) the message and returns what happened. Never throws. */
    EmailStatus send(EmailMessage message);

    /** Convenience for a plain-text email. */
    default EmailStatus send(String to, String subject, String body) {
        return send(EmailMessage.text(to, subject, body));
    }

    /** True when messages really leave through SMTP; false when they are only saved to data/outbox. */
    boolean isDeliveringRealMail();
}
