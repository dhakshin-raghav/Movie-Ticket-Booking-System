package com.moviebooking.service;

import com.moviebooking.model.*;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.strategy.payment.PaymentStrategy;
import com.moviebooking.strategy.pricing.PricingStrategy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrates ticket reservation, seat locking, dynamic pricing, and payment confirmation.
 */
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;
    private final PaymentService paymentService;

    public BookingService(BookingRepository bookingRepository, 
                          SeatLockService seatLockService, 
                          PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.seatLockService = seatLockService;
        this.paymentService = paymentService;
    }

    /**
     * Step 1: Temporarily locks seats and creates a PENDING booking.
     */
    public Booking createBooking(Show show, List<Seat> seats, User user, PricingStrategy pricingStrategy) {
        // 1. Lock seats (throws IllegalStateException if already booked or actively locked)
        seatLockService.lockSeats(show, seats, user);

        // 2. Compute total price using the supplied Pricing Strategy
        double totalAmount = 0.0;
        for (Seat seat : seats) {
            totalAmount += pricingStrategy.calculatePrice(show, seat);
        }

        // 3. Create pending booking
        String bookingId = "BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking(bookingId, show, user, seats, totalAmount);
        bookingRepository.save(booking);

        return booking;
    }

    /**
     * Step 2: Validates lock validity, processes payment, and confirms the booking.
     */
    public Booking confirmBooking(String bookingId, PaymentStrategy paymentStrategy) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking ID not found: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking is not in PENDING state: " + booking.getStatus());
        }

        Show show = booking.getShow();
        User user = booking.getUser();
        List<Seat> seats = booking.getSeats();

        // 1. Verify that lock is still valid (not expired)
        for (Seat seat : seats) {
            if (!seatLockService.validateLock(show, seat, user)) {
                booking.expire();
                seatLockService.unlockSeats(show, seats, user);
                throw new IllegalStateException("Seat lock expired for seat " + seat.getId() + ". Booking could not be completed.");
            }
        }

        // 2. Process payment via Strategy
        Payment payment = paymentService.processPayment(booking.getId(), booking.getTotalAmount(), paymentStrategy);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            // 3. Mark seats permanently booked for the show
            for (Seat seat : seats) {
                show.markSeatBooked(seat.getId());
            }
            // 4. Release temporary locks
            seatLockService.unlockSeats(show, seats, user);
            // 5. Confirm booking
            booking.confirm();
            return booking;
        } else {
            // Payment failed: cancel booking and release locks
            booking.cancel();
            seatLockService.unlockSeats(show, seats, user);
            throw new IllegalStateException("Payment failed. Booking cancelled and seats released.");
        }
    }

    /**
     * Convenience method to lock, pay, and book in a single transaction.
     */
    public Booking bookTickets(Show show, List<Seat> seats, User user, 
                               PricingStrategy pricingStrategy, 
                               PaymentStrategy paymentStrategy) {
        Booking booking = createBooking(show, seats, user, pricingStrategy);
        return confirmBooking(booking.getId(), paymentStrategy);
    }

    public Optional<Booking> getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public List<Booking> getUserBookings(String userId) {
        return bookingRepository.findByUserId(userId);
    }
}
