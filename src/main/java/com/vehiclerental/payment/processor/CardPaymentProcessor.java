package com.vehiclerental.payment.processor;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.common.Validators;
import com.vehiclerental.payment.dto.PaymentForm;
import com.vehiclerental.payment.entity.CardPayment;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentKind;
import com.vehiclerental.payment.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

/** Simulated card payment. 4000 0000 0000 0002 is always declined so a failed payment can be demonstrated. */
@Component
public class CardPaymentProcessor extends AbstractPaymentProcessor {

    public static final String DECLINED_TEST_CARD = "4000000000000002";

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CARD;
    }

    @Override
    public Payment process(Booking booking, PaymentForm form, BigDecimal amount, PaymentKind kind) {
        Map<String, String> errors = new LinkedHashMap<>();
        String digits = form.getCardNumber() == null ? "" : form.getCardNumber().replaceAll("\\s", "");
        if (blank(form.getCardHolder())) {
            errors.put("cardHolder", "Enter the name on the card");
        } else if (form.getCardHolder().trim().length() > 60) {
            errors.put("cardHolder", "Name is too long");
        }
        if (!digits.matches("[0-9]{13,19}")) {
            errors.put("cardNumber", "Card number must be 13 - 19 digits");
        } else if (!Validators.passesLuhn(digits)) {
            errors.put("cardNumber", "That card number is not valid");
        }
        String expiry = form.getExpiry() == null ? "" : form.getExpiry().trim();
        if (!expiry.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            errors.put("expiry", "Use the format MM/YY");
        } else {
            YearMonth card = YearMonth.of(2000 + Integer.parseInt(expiry.substring(3)), Integer.parseInt(expiry.substring(0, 2)));
            if (card.isBefore(YearMonth.now())) {
                errors.put("expiry", "This card has expired");
            }
        }
        if (form.getCvv() == null || !form.getCvv().matches("[0-9]{3,4}")) {
            errors.put("cvv", "CVV must be 3 or 4 digits");
        }
        if (!errors.isEmpty()) {
            throw new PaymentFieldException(errors);
        }
        CardPayment payment = prepare(new CardPayment(), booking, amount, kind);
        payment.setCardHolder(form.getCardHolder().trim());
        payment.setCardLast4(digits.substring(digits.length() - 4));    // the full number and CVV are never stored
        payment.setCardBrand(brandOf(digits));
        if (DECLINED_TEST_CARD.equals(digits)) {
            declined(payment, "Declined by the bank (simulated)");
        }
        return payment;
    }

    private static String brandOf(String digits) {
        char first = digits.charAt(0);
        return switch (first) {
            case '4' -> "Visa";
            case '5', '2' -> "Mastercard";
            case '3' -> "Amex";
            default -> "Card";
        };
    }
}
