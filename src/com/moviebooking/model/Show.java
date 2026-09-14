package com.moviebooking.model;

import java.util.*;

/**
 * Represents a movie screening containing seats and booked seat numbers.
 */
public class Show {
    private final String id;
    private final Movie movie;
    private final String showTime;
    private final List<Seat> seats;
    private final Set<Integer> bookedSeatNumbers = Collections.synchronizedSet(new HashSet<>());

    public Show(String id, Movie movie, String showTime, List<Seat> seats) {
        this.id = id;
        this.movie = movie;
        this.showTime = showTime;
        this.seats = seats;
    }

    public String getId() {
        return id;
    }

    public Movie getMovie() {
        return movie;
    }

    public String getShowTime() {
        return showTime;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public boolean isSeatBooked(int seatNumber) {
        return bookedSeatNumbers.contains(seatNumber);
    }

    public void markSeatBooked(int seatNumber) {
        bookedSeatNumbers.add(seatNumber);
    }

    public Set<Integer> getBookedSeatNumbers() {
        return new HashSet<>(bookedSeatNumbers);
    }

    @Override
    public String toString() {
        return movie.getTitle() + " @ " + showTime;
    }
}
