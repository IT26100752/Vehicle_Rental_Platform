package com.vehiclerental.exception;

/**
 * Thrown when a request is well-formed but breaks a business rule
 * (duplicate email, vehicle not available, booking cannot be cancelled ...).
 * Controllers catch it and show the message to the user.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
