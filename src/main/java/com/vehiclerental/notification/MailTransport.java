package com.vehiclerental.notification;

/**
 * STRATEGY for how an email physically leaves the application.
 * {@link FileMailTransport} writes it to data/outbox, {@link SmtpMailTransport} sends it through an SMTP server.
 */
public interface MailTransport {

    /** Delivers the message or throws {@link MailDeliveryException}. */
    void deliver(EmailMessage message);

    /** Short name stored in the email log, e.g. "SMTP" or "OUTBOX". */
    String name();

    /** True for transports that reach a real inbox. */
    boolean isRealMail();
}
