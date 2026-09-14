package com.moviebooking.strategy;

/**
 * Concrete Strategy: Payment via UPI (GPay, PhonePe, Paytm).
 */
public class UpiPayment implements PaymentStrategy {
    private final String upiId;

    public UpiPayment(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public boolean pay(double amount) {
        System.out.println("[UPI] Charging $" + amount + " via " + upiId + " ... Success! ✅");
        return true;
    }

    @Override
    public String getMethodName() {
        return "UPI (" + upiId + ")";
    }
}
