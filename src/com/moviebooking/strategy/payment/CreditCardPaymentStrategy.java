package com.moviebooking.strategy.payment;

import java.util.UUID;

/**
 * Credit/Debit Card payment strategy implementation.
 */
public class CreditCardPaymentStrategy implements PaymentStrategy {

    private final String cardNumber;
    private final String expiry;
    private final String cvv;
    private String transactionReference;

    public CreditCardPaymentStrategy(String cardNumber, String expiry, String cvv) {
        this.cardNumber = cardNumber;
        this.expiry = expiry;
        this.cvv = cvv;
    }

    @Override
    public boolean processPayment(double amount) {
        if (cardNumber == null || cardNumber.length() < 12 || cvv == null || cvv.length() < 3) {
            System.out.println("[Card] Payment validation failed for card ending in " + getMaskedCardNumber());
            return false;
        }
        this.transactionReference = "CC-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println("[Card] Charging $" + String.format("%.2f", amount) + " to Card " + getMaskedCardNumber() + " ... Success!");
        return true;
    }

    private String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }

    @Override
    public String getPaymentMethodName() {
        return "Credit Card (" + getMaskedCardNumber() + ")";
    }

    @Override
    public String getTransactionReference() {
        return transactionReference;
    }
}
