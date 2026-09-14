package com.moviebooking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a theatre screen / auditorium which contains a collection of physical seats.
 */
public class Screen {
    private final String id;
    private final String name;
    private final List<Seat> seats;

    public Screen(String id, String name) {
        this.id = id;
        this.name = name;
        this.seats = new ArrayList<>();
    }

    public void addSeat(Seat seat) {
        this.seats.add(seat);
    }

    public void addSeats(List<Seat> seats) {
        this.seats.addAll(seats);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    @Override
    public String toString() {
        return "Screen{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", totalSeats=" + seats.size() +
                '}';
    }
}
