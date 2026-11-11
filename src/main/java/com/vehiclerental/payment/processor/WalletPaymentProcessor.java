package com.vehiclerental.payment.processor;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.payment.dto.PaymentForm;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentKind;
import com.vehiclerental.payment.entity.PaymentMethod;
import com.vehiclerental.payment.entity.WalletPayment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Simulated mobile wallet payment (mobile number + PIN). A PIN of 0000 simulates "not enough balance". */
@Component
public class WalletPaymentProcessor extends AbstractPaymentProcessor {

    public static final List<String> PROVIDERS = List.of("eZ Cash", "mCash", "Genie", "FriMi");

    @Override
    public PaymentMethod method() {
        return PaymentMethod.WALLET;
    }

    @Override
    public Payment process(Booking booking, PaymentForm form, BigDecimal amount, PaymentKind kind) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (form.getWalletProvider() == null || !PROVIDERS.contains(form.getWalletProvider())) {
            errors.put("walletProvider", "Choose your wallet");
        }
        String mobile = form.getWalletMobile() == null ? "" : form.getWalletMobile().replaceAll("[\\s-]", "");
        if (!mobile.matches("07[0-9]{8}")) {
            errors.put("walletMobile", "Enter a 10 digit mobile number such as 0771234567");
        }
        if (form.getWalletPin() == null || !form.getWalletPin().matches("[0-9]{4,6}")) {
            errors.put("walletPin", "Enter your 4 to 6 digit wallet PIN");
        }
        if (!errors.isEmpty()) {
            throw new PaymentFieldException(errors);
        }
        WalletPayment payment = prepare(new WalletPayment(), booking, amount, kind);
        payment.setProvider(form.getWalletProvider());
        payment.setMobileMasked("07* *** *" + mobile.substring(mobile.length() - 3));
        if ("0000".equals(form.getWalletPin())) {
            declined(payment, "Not enough balance in the wallet (simulated)");
        }
        return payment;
    }
}
