package com.vehiclerental.booking.service;

/** Published when a booking is cancelled so the payment module can refund it (loose coupling). */
public class BookingCancelledEvent {

    private final Long bookingId;

    public BookingCancelledEvent(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getBookingId() {
        return bookingId;
    }
}
