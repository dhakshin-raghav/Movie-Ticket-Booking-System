package com.moviebooking.strategy.payment;

import java.util.UUID;

/**
 * UPI payment strategy implementation (Google Pay, PhonePe, Paytm, etc.).
 */
public class UpiPaymentStrategy implements PaymentStrategy {

    private final String upiId;
    private String transactionReference;

    public UpiPaymentStrategy(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public boolean processPayment(double amount) {
        if (upiId == null || !upiId.contains("@")) {
            System.out.println("[UPI] Invalid UPI ID: " + upiId);
            return false;
        }
        this.transactionReference = "UPI-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println("[UPI] Processing payment of $" + String.format("%.2f", amount) + " via " + upiId + " ... Success!");
        return true;
    }

    @Override
    public String getPaymentMethodName() {
        return "UPI (" + upiId + ")";
    }

    @Override
    public String getTransactionReference() {
        return transactionReference;
    }
}
