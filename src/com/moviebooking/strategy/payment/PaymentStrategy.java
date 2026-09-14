package com.moviebooking.strategy.payment;

/**
 * Strategy pattern interface for processing payments.
 * Decouples the booking workflow from payment gateway integrations.
 */
public interface PaymentStrategy {
    /**
     * Executes the payment transaction.
     * @param amount the total amount to charge
     * @return true if payment succeeded, false otherwise
     */
    boolean processPayment(double amount);

    /**
     * Returns the name/description of this payment method.
     */
    String getPaymentMethodName();

    /**
     * Returns the unique transaction reference ID generated for this payment.
     */
    String getTransactionReference();
}
