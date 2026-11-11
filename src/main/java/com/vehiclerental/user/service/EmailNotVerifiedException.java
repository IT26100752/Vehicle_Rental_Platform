package com.vehiclerental.user.service;

import com.vehiclerental.exception.BusinessRuleException;

/** Sign-in attempted with a correct password but an unconfirmed email address. */
public class EmailNotVerifiedException extends BusinessRuleException {

    private final String email;

    public EmailNotVerifiedException(String email) {
        super("Please verify your email address before signing in.");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
