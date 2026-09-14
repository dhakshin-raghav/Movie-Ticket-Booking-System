package com.moviebooking.service;

import com.moviebooking.model.Show;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service providing in-memory seat locking to prevent race conditions.
 * Ensures that two concurrent users/threads cannot book the exact same seat simultaneously.
 */
public class SeatLockService {

    // In-memory lock map: "showId:seatNumber" -> lockedByUserName
    private final Map<String, String> lockedSeats = new ConcurrentHashMap<>();
    private final ReentrantLock mutex = new ReentrantLock();

    /**
     * Atomically locks a seat for a user.
     * Returns true if lock was acquired; false if already locked or booked.
     */
    public boolean lockSeat(Show show, int seatNumber, String userName) {
        mutex.lock(); // Ensure only one thread at a time can check and acquire the lock
        try {
            // 1. Check if the seat is already permanently booked
            if (show.isSeatBooked(seatNumber)) {
                return false;
            }

            // 2. Check if the seat is currently locked by another user
            String key = getLockKey(show.getId(), seatNumber);
            if (lockedSeats.containsKey(key)) {
                return false;
            }

            // 3. Acquire temporary lock
            lockedSeats.put(key, userName);
            return true;
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Releases the in-memory seat lock.
     */
    public void unlockSeat(Show show, int seatNumber) {
        String key = getLockKey(show.getId(), seatNumber);
        lockedSeats.remove(key);
    }

    /**
     * Checks if a seat is currently locked.
     */
    public boolean isSeatLocked(Show show, int seatNumber) {
        return lockedSeats.containsKey(getLockKey(show.getId(), seatNumber));
    }

    private String getLockKey(String showId, int seatNumber) {
        return showId + ":" + seatNumber;
    }
}
