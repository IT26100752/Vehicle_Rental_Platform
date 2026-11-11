package com.vehiclerental.payment.dto;

import com.vehiclerental.payment.entity.PaymentMethod;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Everything the payment page can send. Which fields matter depends on the chosen method, so the checks live in
 * the matching PaymentProcessor (Strategy pattern) instead of in annotations here.
 * Card number, CVV and wallet PIN are validated and then thrown away - they are never stored.
 */
public class PaymentForm {

    private PaymentMethod method = PaymentMethod.CARD;

    // card
    private String cardHolder;
    private String cardNumber;
    private String expiry;
    private String cvv;

    // wallet
    private String walletProvider;
    private String walletMobile;
    private String walletPin;

    // bank transfer
    private String bankName;
    private String slipReference;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate transferDate;

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method == null ? PaymentMethod.CARD : method; }
    public String getCardHolder() { return cardHolder; }
    public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    public String getWalletProvider() { return walletProvider; }
    public void setWalletProvider(String walletProvider) { this.walletProvider = walletProvider; }
    public String getWalletMobile() { return walletMobile; }
    public void setWalletMobile(String walletMobile) { this.walletMobile = walletMobile; }
    public String getWalletPin() { return walletPin; }
    public void setWalletPin(String walletPin) { this.walletPin = walletPin; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getSlipReference() { return slipReference; }
    public void setSlipReference(String slipReference) { this.slipReference = slipReference; }
    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }
}
