package com.moviebooking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Represents a ticket reservation placed by a user.
 */
public class Booking {
    private final String id;
    private final Show show;
    private final User user;
    private final List<Seat> seats;
    private final double totalAmount;
    private BookingStatus status;
    private final LocalDateTime createdAt;

    public Booking(String id, Show show, User user, List<Seat> seats, double totalAmount) {
        this.id = id;
        this.show = show;
        this.user = user;
        this.seats = seats;
        this.totalAmount = totalAmount;
        this.status = BookingStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public Show getShow() {
        return show;
    }

    public User getUser() {
        return user;
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public void expire() {
        this.status = BookingStatus.EXPIRED;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder seatIds = new StringBuilder();
        for (Seat s : seats) {
            seatIds.append(s.getId()).append(" ");
        }
        return "Booking Receipt [" + id + "]\n" +
                "  Status: " + status + "\n" +
                "  User: " + user.getName() + " (" + user.getEmail() + ")\n" +
                "  Movie: " + show.getMovie().getTitle() + "\n" +
                "  Theatre: " + show.getCinemaHall().getName() + " | " + show.getScreen().getName() + "\n" +
                "  Seats: " + seatIds.toString().trim() + "\n" +
                "  Total Amount: $" + String.format("%.2f", totalAmount) + "\n" +
                "  Booked At: " + createdAt.format(formatter);
    }
}
