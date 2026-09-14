package com.moviebooking.controller;

import com.moviebooking.model.Booking;
import com.moviebooking.model.Seat;
import com.moviebooking.model.Show;
import com.moviebooking.model.User;
import com.moviebooking.service.BookingService;
import com.moviebooking.strategy.payment.PaymentStrategy;
import com.moviebooking.strategy.pricing.PricingStrategy;

import java.util.List;
import java.util.Optional;

/**
 * Controller exposing booking workflows.
 */
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public Booking bookTickets(Show show, List<Seat> seats, User user, 
                               PricingStrategy pricingStrategy, 
                               PaymentStrategy paymentStrategy) {
        return bookingService.bookTickets(show, seats, user, pricingStrategy, paymentStrategy);
    }

    public Booking createBooking(Show show, List<Seat> seats, User user, PricingStrategy pricingStrategy) {
        return bookingService.createBooking(show, seats, user, pricingStrategy);
    }

    public Booking confirmBooking(String bookingId, PaymentStrategy paymentStrategy) {
        return bookingService.confirmBooking(bookingId, paymentStrategy);
    }

    public Optional<Booking> getBooking(String bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    public List<Booking> getUserBookings(String userId) {
        return bookingService.getUserBookings(userId);
    }
}
