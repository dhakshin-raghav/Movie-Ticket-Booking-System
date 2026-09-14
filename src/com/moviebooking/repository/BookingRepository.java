package com.moviebooking.repository;

import com.moviebooking.model.Booking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory repository for storing and querying Bookings.
 */
public class BookingRepository {
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    public void save(Booking booking) {
        bookings.put(booking.getId(), booking);
    }

    public Optional<Booking> findById(String id) {
        return Optional.ofNullable(bookings.get(id));
    }

    public List<Booking> findAll() {
        return new ArrayList<>(bookings.values());
    }

    public List<Booking> findByUserId(String userId) {
        return bookings.values().stream()
                .filter(b -> b.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }
}
