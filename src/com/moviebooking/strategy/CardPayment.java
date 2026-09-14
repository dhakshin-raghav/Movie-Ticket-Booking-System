package com.moviebooking.strategy;

/**
 * Concrete Strategy: Payment via Credit or Debit Card.
 */
public class CardPayment implements PaymentStrategy {
    private final String cardNumber;

    public CardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public boolean pay(double amount) {
        String masked = cardNumber.length() > 4 ? "****" + cardNumber.substring(cardNumber.length() - 4) : cardNumber;
        System.out.println("[Card] Charging $" + amount + " to card ending in " + masked + " ... Success! ✅");
        return true;
    }

    @Override
    public String getMethodName() {
        return "Card Payment";
    }
}
