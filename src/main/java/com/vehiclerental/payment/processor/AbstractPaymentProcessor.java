package com.vehiclerental.payment.processor;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentKind;
import com.vehiclerental.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Shared bits: fill the fields every payment has, and collect field errors. */
abstract class AbstractPaymentProcessor implements PaymentProcessor {

    protected <T extends Payment> T prepare(T payment, Booking booking, BigDecimal amount, PaymentKind kind) {
        payment.setBookingId(booking.getId());
        payment.setCustomerId(booking.getCustomerId());
        payment.setAmount(amount);
        payment.setKind(kind);
        payment.setReference("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        return payment;
    }

    protected static void declined(Payment payment, String reason) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
    }

    protected static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
