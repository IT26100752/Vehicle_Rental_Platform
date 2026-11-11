package com.vehiclerental.payment.processor;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.payment.dto.PaymentForm;
import com.vehiclerental.payment.entity.CashPayment;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentKind;
import com.vehiclerental.payment.entity.PaymentMethod;
import com.vehiclerental.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * "Pay with cash": the customer reserves the vehicle online and hands the money over at the counter.
 * Nothing can be checked automatically, so the payment waits as PENDING_VERIFICATION until a staff member
 * confirms that the cash was received (the same review flow as a bank transfer).
 */
@Component
public class CashPaymentProcessor extends AbstractPaymentProcessor {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CASH;
    }

    @Override
    public boolean supportsFines() {
        return false;
    }

    @Override
    public Payment process(Booking booking, PaymentForm form, BigDecimal amount, PaymentKind kind) {
        CashPayment payment = prepare(new CashPayment(), booking, amount, kind);
        payment.setStatus(PaymentStatus.PENDING_VERIFICATION);
        payment.setPaidAt(null);
        return payment;
    }
}
