package com.moviebooking.service;

import com.moviebooking.model.Payment;
import com.moviebooking.model.PaymentStatus;
import com.moviebooking.strategy.payment.PaymentStrategy;

import java.util.UUID;

/**
 * Service that delegates payment execution to the selected PaymentStrategy.
 */
public class PaymentService {

    public Payment processPayment(String bookingId, double amount, PaymentStrategy paymentStrategy) {
        boolean success = paymentStrategy.processPayment(amount);
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        PaymentStatus status = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        String ref = paymentStrategy.getTransactionReference() != null ? 
                paymentStrategy.getTransactionReference() : "FAILED";

        return new Payment(paymentId, bookingId, amount, status, ref);
    }
}
