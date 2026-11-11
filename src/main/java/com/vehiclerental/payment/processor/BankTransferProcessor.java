package com.vehiclerental.payment.processor;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.payment.dto.PaymentForm;
import com.vehiclerental.payment.entity.BankTransferPayment;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentKind;
import com.vehiclerental.payment.entity.PaymentMethod;
import com.vehiclerental.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Customer transfers money from their own bank and tells us the slip number. We cannot check the bank account
 * automatically, so the payment waits as PENDING_VERIFICATION until an administrator approves or rejects it.
 */
@Component
public class BankTransferProcessor extends AbstractPaymentProcessor {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.BANK_TRANSFER;
    }

    @Override
    public boolean supportsFines() {
        return false;
    }

    @Override
    public Payment process(Booking booking, PaymentForm form, BigDecimal amount, PaymentKind kind) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (blank(form.getBankName())) {
            errors.put("bankName", "Enter the name of your bank");
        } else if (form.getBankName().trim().length() > 50) {
            errors.put("bankName", "Bank name is too long");
        }
        if (form.getSlipReference() == null || !form.getSlipReference().trim().matches("[A-Za-z0-9\\-]{6,30}")) {
            errors.put("slipReference", "Enter the transfer reference from your slip (6 - 30 letters or digits)");
        }
        LocalDate date = form.getTransferDate();
        if (date == null) {
            errors.put("transferDate", "Choose the date of the transfer");
        } else if (date.isAfter(LocalDate.now())) {
            errors.put("transferDate", "The transfer date cannot be in the future");
        } else if (date.isBefore(LocalDate.now().minusDays(7))) {
            errors.put("transferDate", "Transfers older than 7 days cannot be used");
        }
        if (!errors.isEmpty()) {
            throw new PaymentFieldException(errors);
        }
        BankTransferPayment payment = prepare(new BankTransferPayment(), booking, amount, kind);
        payment.setBankName(form.getBankName().trim());
        payment.setSlipReference(form.getSlipReference().trim().toUpperCase());
        payment.setTransferDate(date);
        payment.setStatus(PaymentStatus.PENDING_VERIFICATION);
        payment.setPaidAt(null);
        return payment;
    }
}
