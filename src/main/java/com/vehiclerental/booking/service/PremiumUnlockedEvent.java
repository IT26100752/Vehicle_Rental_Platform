package com.vehiclerental.booking.service;

/** Published when a customer's bookings pass the limit and the account becomes PREMIUM. */
public class PremiumUnlockedEvent {

    private final Long customerId;

    public PremiumUnlockedEvent(Long customerId) {
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
