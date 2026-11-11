package com.vehiclerental.booking.service;

/**
 * Published when a booking becomes CONFIRMED (paid or bank transfer approved).
 * The driver module listens to it to assign and notify a driver (loose coupling, like BookingCancelledEvent).
 */
public class BookingConfirmedEvent {

    private final Long bookingId;

    public BookingConfirmedEvent(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getBookingId() {
        return bookingId;
    }
}
