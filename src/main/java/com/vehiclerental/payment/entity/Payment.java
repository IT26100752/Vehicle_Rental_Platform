package com.vehiclerental.payment.entity;

import com.vehiclerental.common.BaseEntity;

import java.math.BigDecimal;

/**
 * Abstract payment record. CardPayment and CashPayment extend it.
 * POLYMORPHISM: callers use getMethod()/getMethodDetails() without caring which subtype they hold.
 */
public abstract class Payment extends BaseEntity {

    private Long bookingId;
    private Long customerId;
    private BigDecimal amount = BigDecimal.ZERO;
    private PaymentKind kind = PaymentKind.RENTAL;
    private PaymentStatus status = PaymentStatus.PAID;
    private String reference;
    private String receiptNo;
    private java.time.LocalDateTime paidAt;
    private String failureReason;
    private String verifiedBy;

    /** Which payment method this record is (each subclass answers for itself). */
    public abstract PaymentMethod getMethodType();

    /** e.g. "Card" or "Cash". */
    public String getMethod() {
        return getMethodType().getLabel();
    }

    /** e.g. "Visa ending 4242" or "Received by admin". */
    public abstract String getMethodDetails();

    /** The moment the payment was taken. */
    public java.time.LocalDateTime getPaidAt() {
        return paidAt != null ? paidAt : getCreatedAt();
    }

    public void setPaidAt(java.time.LocalDateTime paidAt) { this.paidAt = paidAt; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    /** A receipt exists once the money is in (or was returned). */
    public boolean isReceiptAvailable() {
        return status == PaymentStatus.PAID || status == PaymentStatus.REFUNDED;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative");
        }
        this.amount = amount;
    }

    public PaymentKind getKind() {
        return kind;
    }

    public void setKind(PaymentKind kind) {
        this.kind = kind == null ? PaymentKind.RENTAL : kind;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status == null ? PaymentStatus.PAID : status;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
