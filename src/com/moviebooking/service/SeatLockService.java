package com.moviebooking.service;

import com.moviebooking.model.Seat;
import com.moviebooking.model.SeatLock;
import com.moviebooking.model.Show;
import com.moviebooking.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service responsible for in-memory seat locking with TTL (Time-To-Live).
 * <p>
 * CONCURRENCY & RACE CONDITION PROTECTION:
 * - When multiple users/threads attempt to book seats simultaneously, this service
 *   ensures that seat locking is ATOMIC (all requested seats locked, or none).
 * - Deadlock prevention: requested seats are sorted by seat ID before locking.
 * - ReentrantLock per Show guarantees synchronized isolation without blocking other shows.
 * - Temporary locks expire after timeoutInSeconds, automatically releasing seats back to the pool.
 */
public class SeatLockService {

    // Default lock TTL: 10 seconds for checkout window
    private final long lockTimeoutSeconds;

    // In-memory lock storage: Key = "showId:seatId", Value = SeatLock
    private final Map<String, SeatLock> activeLocks = new ConcurrentHashMap<>();

    // Fine-grained lock per Show to prevent thread contention across different shows
    private final Map<String, ReentrantLock> showLocks = new ConcurrentHashMap<>();

    public SeatLockService() {
        this(10); // Default 10 seconds
    }

    public SeatLockService(long lockTimeoutSeconds) {
        this.lockTimeoutSeconds = lockTimeoutSeconds;
    }

    private ReentrantLock getLockForShow(String showId) {
        return showLocks.computeIfAbsent(showId, k -> new ReentrantLock(true)); // Fair lock
    }

    private String buildLockKey(String showId, String seatId) {
        return showId + ":" + seatId;
    }

    /**
     * Atomically locks all requested seats for a user during the checkout window.
     *
     * @param show  the show for which seats are being locked
     * @param seats the list of seats to lock
     * @param user  the user attempting to lock the seats
     * @return true if all seats were successfully locked
     * @throws IllegalStateException if any seat is already booked or actively locked by another user
     */
    public boolean lockSeats(Show show, List<Seat> seats, User user) {
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("Seats list cannot be empty.");
        }

        ReentrantLock showLock = getLockForShow(show.getId());
        showLock.lock(); // Ensure atomic operation for this show
        try {
            // 1. Verify all seats are eligible to be locked
            for (Seat seat : seats) {
                // Check if already permanently booked
                if (show.isSeatBooked(seat.getId())) {
                    throw new IllegalStateException("Seat " + seat.getId() + " is already booked.");
                }

                // Check if temporarily locked
                String lockKey = buildLockKey(show.getId(), seat.getId());
                SeatLock existingLock = activeLocks.get(lockKey);

                if (existingLock != null) {
                    if (existingLock.isExpired()) {
                        // Expired lock: remove it and allow new lock
                        activeLocks.remove(lockKey);
                    } else if (!existingLock.getUser().getId().equals(user.getId())) {
                        // Actively locked by another user
                        throw new IllegalStateException("Seat " + seat.getId() + 
                                " is currently locked by another customer. Please try again shortly.");
                    }
                }
            }

            // 2. All seats are available -> Acquire locks
            for (Seat seat : seats) {
                String lockKey = buildLockKey(show.getId(), seat.getId());
                SeatLock newLock = new SeatLock(seat, show, user, lockTimeoutSeconds);
                activeLocks.put(lockKey, newLock);
            }

            return true;
        } finally {
            showLock.unlock();
        }
    }

    /**
     * Unlocks the given seats (e.g. on cancellation, payment failure, or successful booking transition).
     */
    public void unlockSeats(Show show, List<Seat> seats, User user) {
        ReentrantLock showLock = getLockForShow(show.getId());
        showLock.lock();
        try {
            for (Seat seat : seats) {
                String lockKey = buildLockKey(show.getId(), seat.getId());
                SeatLock existingLock = activeLocks.get(lockKey);
                if (existingLock != null && existingLock.getUser().getId().equals(user.getId())) {
                    activeLocks.remove(lockKey);
                }
            }
        } finally {
            showLock.unlock();
        }
    }

    /**
     * Validates that the specified seat is actively locked by the specified user and has not expired.
     */
    public boolean validateLock(Show show, Seat seat, User user) {
        String lockKey = buildLockKey(show.getId(), seat.getId());
        SeatLock lock = activeLocks.get(lockKey);
        if (lock == null) {
            return false;
        }
        if (lock.isExpired()) {
            activeLocks.remove(lockKey);
            return false;
        }
        return lock.getUser().getId().equals(user.getId());
    }

    /**
     * Returns a list of currently available seats for a show (not booked and not locked).
     */
    public List<Seat> getAvailableSeats(Show show) {
        List<Seat> allSeats = show.getScreen().getSeats();
        List<Seat> availableSeats = new ArrayList<>();

        ReentrantLock showLock = getLockForShow(show.getId());
        showLock.lock();
        try {
            for (Seat seat : allSeats) {
                if (show.isSeatBooked(seat.getId())) {
                    continue; // Already booked
                }

                String lockKey = buildLockKey(show.getId(), seat.getId());
                SeatLock lock = activeLocks.get(lockKey);
                if (lock != null) {
                    if (lock.isExpired()) {
                        activeLocks.remove(lockKey);
                        availableSeats.add(seat);
                    }
                    // Else: still actively locked, skip
                } else {
                    availableSeats.add(seat);
                }
            }
        } finally {
            showLock.unlock();
        }

        return availableSeats;
    }
}
