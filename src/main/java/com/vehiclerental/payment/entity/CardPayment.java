package com.vehiclerental.payment.entity;

/**
 * Simulated card payment. Only the LAST FOUR digits are ever stored -
 * the full card number and the CVV are validated and then discarded.
 */
public class CardPayment extends Payment {

    private String cardHolder;
    private String cardLast4;
    private String cardBrand;

    @Override
    public PaymentMethod getMethodType() {
        return PaymentMethod.CARD;
    }

    @Override
    public String getMethodDetails() {
        return cardBrand + " ending " + cardLast4;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }
}
