package com.moviebooking.model;

import java.time.LocalDateTime;

/**
 * Represents a payment transaction made for a booking.
 */
public class Payment {
    private final String id;
    private final String bookingId;
    private final double amount;
    private final PaymentStatus status;
    private final String transactionReference;
    private final LocalDateTime timestamp;

    public Payment(String id, String bookingId, double amount, PaymentStatus status, String transactionReference) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.transactionReference = transactionReference;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id='" + id + '\'' +
                ", bookingId='" + bookingId + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                ", ref='" + transactionReference + '\'' +
                '}';
    }
}
