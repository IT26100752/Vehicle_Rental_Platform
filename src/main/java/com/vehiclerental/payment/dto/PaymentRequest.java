package com.vehiclerental.payment.dto;

import com.vehiclerental.payment.entity.PaymentMethod;

/**
 * Data carried by the 'Pay' button on a booking.
 */
public class PaymentRequest {

    private int bookingId;
    private PaymentMethod method;

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
}
