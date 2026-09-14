package com.moviebooking.model;

/**
 * Represents an individual seat in a movie theatre screen.
 */
public class Seat {
    private final int seatNumber;
    private final String seatType; // Silver, Gold, Platinum
    private final double price;

    public Seat(int seatNumber, String seatType, double price) {
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.price = price;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Seat " + seatNumber + " [" + seatType + " - $" + price + "]";
    }
}
