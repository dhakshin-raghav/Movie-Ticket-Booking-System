package com.moviebooking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a specific screening of a Movie in a Screen at a scheduled time.
 */
public class Show {
    private final String id;
    private final Movie movie;
    private final CinemaHall cinemaHall;
    private final Screen screen;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    // Track booked seats in a thread-safe manner for this show
    private final Set<String> bookedSeatIds;

    public Show(String id, Movie movie, CinemaHall cinemaHall, Screen screen, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.movie = movie;
        this.cinemaHall = cinemaHall;
        this.screen = screen;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookedSeatIds = Collections.synchronizedSet(new HashSet<>());
    }

    public String getId() {
        return id;
    }

    public Movie getMovie() {
        return movie;
    }

    public CinemaHall getCinemaHall() {
        return cinemaHall;
    }

    public Screen getScreen() {
        return screen;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isSeatBooked(String seatId) {
        return bookedSeatIds.contains(seatId);
    }

    public void markSeatBooked(String seatId) {
        bookedSeatIds.add(seatId);
    }

    public Set<String> getBookedSeatIds() {
        synchronized (bookedSeatIds) {
            return new HashSet<>(bookedSeatIds);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Show show = (Show) o;
        return Objects.equals(id, show.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "Show #" + id + " | " + movie.getTitle() + " | " + cinemaHall.getName() + " - " + screen.getName() +
                " | " + startTime.format(formatter);
    }
}
