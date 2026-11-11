package com.vehiclerental.payment.entity;

import java.time.LocalDate;

/** Customer says they paid by bank transfer; stays PENDING_VERIFICATION until an admin checks the bank account. */
public class BankTransferPayment extends Payment {

    private String bankName;
    private String slipReference;
    private LocalDate transferDate;

    @Override
    public PaymentMethod getMethodType() {
        return PaymentMethod.BANK_TRANSFER;
    }

    @Override
    public String getMethodDetails() {
        return bankName + ", slip " + slipReference;
    }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getSlipReference() { return slipReference; }
    public void setSlipReference(String slipReference) { this.slipReference = slipReference; }
    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }
}
