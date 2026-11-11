package com.vehiclerental.payment.entity;

/** Simulated mobile wallet payment. Only the provider and the last 3 digits of the number are stored. */
public class WalletPayment extends Payment {

    private String provider;
    private String mobileMasked;

    @Override
    public PaymentMethod getMethodType() {
        return PaymentMethod.WALLET;
    }

    @Override
    public String getMethodDetails() {
        return provider + " wallet " + mobileMasked;
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getMobileMasked() { return mobileMasked; }
    public void setMobileMasked(String mobileMasked) { this.mobileMasked = mobileMasked; }
}
