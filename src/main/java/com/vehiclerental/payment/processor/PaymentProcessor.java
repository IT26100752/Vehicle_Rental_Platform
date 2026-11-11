package com.vehiclerental.payment.processor;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.payment.dto.PaymentForm;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentKind;
import com.vehiclerental.payment.entity.PaymentMethod;

import java.math.BigDecimal;

/**
 * STRATEGY: one implementation per payment method. PaymentService picks the right one by {@link #method()}
 * and never needs to know how a card, a wallet or a bank transfer is checked.
 *
 * process() validates the form and returns an UNSAVED payment whose status is PAID (money taken),
 * PENDING_VERIFICATION (an admin must confirm) or FAILED (declined, with a reason). Bad input throws
 * {@link PaymentFieldException}.
 */
public interface PaymentProcessor {

    PaymentMethod method();

    /** Can this method also be used to pay a late fine? (bank transfers cannot - fines are settled on the spot) */
    default boolean supportsFines() {
        return true;
    }

    Payment process(Booking booking, PaymentForm form, BigDecimal amount, PaymentKind kind);
}
