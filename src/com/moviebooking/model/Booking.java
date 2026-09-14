package com.moviebooking.model;

/**
 * Represents a confirmed ticket booking for a user.
 */
public class Booking {
    private final String id;
    private final Show show;
    private final int seatNumber;
    private final User user;
    private final double totalAmount;

    public Booking(String id, Show show, int seatNumber, User user, double totalAmount) {
        this.id = id;
        this.show = show;
        this.seatNumber = seatNumber;
        this.user = user;
        this.totalAmount = totalAmount;
    }

    public String getId() {
        return id;
    }

    public Show getShow() {
        return show;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public User getUser() {
        return user;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String toString() {
        return "\n========================================" +
               "\n🎟️       MOVIE TICKET RECEIPT           " +
               "\n========================================" +
               "\nBooking ID : " + id +
               "\nMovie      : " + show.getMovie().getTitle() +
               "\nShow Time  : " + show.getShowTime() +
               "\nSeat No    : " + seatNumber +
               "\nCustomer   : " + user.getName() +
               "\nTotal Paid : $" + totalAmount +
               "\nStatus     : CONFIRMED ✅" +
               "\n========================================\n";
    }
}
