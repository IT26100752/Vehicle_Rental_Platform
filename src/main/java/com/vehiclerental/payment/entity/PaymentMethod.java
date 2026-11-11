package com.vehiclerental.payment.entity;

/** The ways a rental can be paid. CASH is reserved online and handed over at the counter; staff confirm it. */
public enum PaymentMethod {
    CARD("Card", "Visa or Mastercard, confirmed instantly", true),
    WALLET("Mobile wallet", "eZ Cash, mCash, Genie or FriMi, confirmed instantly", true),
    BANK_TRANSFER("Bank transfer", "Pay from your bank, we confirm when the money arrives", true),
    CASH("Pay with cash", "Pay in cash at the counter when you collect the vehicle", true);

    private final String label;
    private final String blurb;
    private final boolean online;

    PaymentMethod(String label, String blurb, boolean online) {
        this.label = label;
        this.blurb = blurb;
        this.online = online;
    }

    public String getLabel() { return label; }
    public String getBlurb() { return blurb; }
    public boolean isOnline() { return online; }
}
