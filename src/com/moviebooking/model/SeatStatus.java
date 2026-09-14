package com.moviebooking.model;

/**
 * Represents the current status of a seat for a specific show.
 * - AVAILABLE: Any user can select and lock this seat.
 * - LOCKED: Temporarily held by a user during the checkout/payment window.
 * - BOOKED: Permanently booked after successful payment.
 */
public enum SeatStatus {
    AVAILABLE,
    LOCKED,
    BOOKED
}
