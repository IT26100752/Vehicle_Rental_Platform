package com.vehiclerental.payment.processor;

import com.vehiclerental.exception.BusinessRuleException;

import java.util.LinkedHashMap;
import java.util.Map;

/** The payment details were not valid. Carries one message per form field so the page can mark each box. */
public class PaymentFieldException extends BusinessRuleException {

    private final Map<String, String> errors;

    public PaymentFieldException(Map<String, String> errors) {
        super(errors.values().stream().findFirst().orElse("Please check the payment details"));
        this.errors = new LinkedHashMap<>(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
