package com.moviebooking.controller;

import com.moviebooking.model.Booking;
import com.moviebooking.model.Show;
import com.moviebooking.model.User;
import com.moviebooking.service.BookingService;
import com.moviebooking.strategy.PaymentStrategy;

import java.util.List;

/**
 * Controller layer exposing simplified APIs to the console UI.
 */
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public List<Show> getAllShows() {
        return bookingService.getShows();
    }

    public Booking bookTicket(Show show, int seatNumber, User user, PaymentStrategy paymentStrategy) {
        return bookingService.bookTicket(show, seatNumber, user, paymentStrategy);
    }
}
