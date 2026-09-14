package com.moviebooking.service;

import com.moviebooking.model.*;
import com.moviebooking.strategy.PaymentStrategy;

import java.util.*;

/**
 * Service that orchestrates seat locking, payments, and ticket generation.
 */
public class BookingService {

    private final SeatLockService seatLockService;
    private final List<Show> shows = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    public BookingService(SeatLockService seatLockService) {
        this.seatLockService = seatLockService;
        initializeSampleData();
    }

    /**
     * Attempts to book a seat using thread-safe locking and a chosen Payment Strategy.
     */
    public Booking bookTicket(Show show, int seatNumber, User user, PaymentStrategy paymentStrategy) {
        // Step 1: Attempt to acquire the in-memory seat lock to prevent race conditions
        boolean locked = seatLockService.lockSeat(show, seatNumber, user.getName());
        if (!locked) {
            throw new IllegalStateException("Seat " + seatNumber + " is unavailable or already locked by another user!");
        }

        try {
            // Find seat price
            Seat targetSeat = show.getSeats().stream()
                    .filter(s -> s.getSeatNumber() == seatNumber)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Seat " + seatNumber + " does not exist."));

            // Step 2: Process payment via Strategy Pattern
            boolean paid = paymentStrategy.pay(targetSeat.getPrice());
            if (!paid) {
                throw new IllegalStateException("Payment failed!");
            }

            // Step 3: Mark seat permanently booked
            show.markSeatBooked(seatNumber);

            // Step 4: Create booking record
            String bookingId = "BKG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            Booking booking = new Booking(bookingId, show, seatNumber, user, targetSeat.getPrice());
            bookings.add(booking);

            return booking;
        } finally {
            // Step 5: Always release the temporary lock once transaction completes
            seatLockService.unlockSeat(show, seatNumber);
        }
    }

    public List<Show> getShows() {
        return shows;
    }

    public SeatLockService getSeatLockService() {
        return seatLockService;
    }

    private void initializeSampleData() {
        // Create 10 seats for screens
        List<Seat> screenSeats = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            screenSeats.add(new Seat(i, "SILVER", 150.0));
        }
        for (int i = 5; i <= 8; i++) {
            screenSeats.add(new Seat(i, "GOLD", 250.0));
        }
        for (int i = 9; i <= 10; i++) {
            screenSeats.add(new Seat(i, "PLATINUM", 400.0));
        }

        Movie m1 = new Movie("M1", "Interstellar", 169);
        Movie m2 = new Movie("M2", "Inception", 148);

        shows.add(new Show("S1", m1, "06:30 PM", screenSeats));
        shows.add(new Show("S2", m2, "09:15 PM", screenSeats));
    }
}
