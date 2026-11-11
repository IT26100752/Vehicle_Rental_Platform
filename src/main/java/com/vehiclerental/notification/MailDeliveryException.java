package com.vehiclerental.notification;

/** A transport could not deliver an email (wrong password, no network, disk full ...). */
public class MailDeliveryException extends RuntimeException {

    public MailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
