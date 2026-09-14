package com.moviebooking.strategy;

/**
 * Strategy pattern interface for pluggable payment processing.
 */
public interface PaymentStrategy {
    boolean pay(double amount);
    String getMethodName();
}
