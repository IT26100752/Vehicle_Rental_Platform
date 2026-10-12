package com.vehiclerental.payment.entity;

import com.vehiclerental.common.entity.Identifiable;

import java.time.LocalDate;

/**
 * PAYMENT (Member 4's entity, field list from the intro session)
 *   paymentId, bookingId, amount, paymentMethod, paymentDate,
 *   paymentStatus, transactionReference
 */
public class Payment implements Identifiable {

    private int id;                    // paymentId
    private int bookingId;
    private double amount;
    private PaymentMethod method;      // CARD / CASH / ONLINE (simulated)
    private LocalDate paymentDate;
    private PaymentStatus status;
    private String transactionReference;  // e.g. TXN-9F3A21

    public Payment() {
    }

    public Payment(int id, int bookingId, double amount, PaymentMethod method,
                   LocalDate paymentDate, PaymentStatus status, String transactionReference) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.method = method;
        this.paymentDate = paymentDate;
        this.status = status;
        this.transactionReference = transactionReference;
    }

    /* ------------------------- getters / setters ---------------------- */
    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    @Override
    public String toString() {
        return "Payment #" + id + " " + transactionReference + " LKR " + amount
                + " [" + status + "]";
    }
}
