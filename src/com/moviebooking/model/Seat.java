package com.moviebooking.model;

import java.util.Objects;

/**
 * Represents an individual physical seat inside a cinema Screen.
 */
public class Seat {
    private final String id;
    private final String row;
    private final int seatNumber;
    private final SeatType seatType;

    public Seat(String id, String row, int seatNumber, SeatType seatType) {
        this.id = id;
        this.row = row;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public String getId() {
        return id;
    }

    public String getRow() {
        return row;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return Objects.equals(id, seat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "[" + id + " (" + seatType + ")]";
    }
}
