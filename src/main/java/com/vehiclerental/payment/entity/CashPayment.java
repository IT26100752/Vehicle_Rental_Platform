package com.vehiclerental.payment.entity;

/** Payment taken at the counter and recorded by an administrator. */
public class CashPayment extends Payment {

    private String receivedBy;

    @Override
    public PaymentMethod getMethodType() {
        return PaymentMethod.CASH;
    }

    @Override
    public String getMethodDetails() {
        return receivedBy == null || receivedBy.isBlank()
                ? "Cash, to be paid at the counter"
                : "Cash received at counter by " + receivedBy;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }
}
