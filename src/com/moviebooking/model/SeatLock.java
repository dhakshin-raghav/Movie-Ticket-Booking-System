package com.moviebooking.model;

import java.time.Instant;

/**
 * Represents a temporary in-memory lock on a seat for a specific show by a user.
 * This lock prevents race conditions during the checkout and payment process.
 */
public class SeatLock {
    private final Seat seat;
    private final Show show;
    private final User user;
    private final Instant lockTime;
    private final long timeoutInSeconds;

    public SeatLock(Seat seat, Show show, User user, long timeoutInSeconds) {
        this.seat = seat;
        this.show = show;
        this.user = user;
        this.lockTime = Instant.now();
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public Seat getSeat() {
        return seat;
    }

    public Show getShow() {
        return show;
    }

    public User getUser() {
        return user;
    }

    public Instant getLockTime() {
        return lockTime;
    }

    public long getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    /**
     * Checks if this lock has expired based on its TTL (Time-To-Live).
     */
    public boolean isExpired() {
        return Instant.now().isAfter(lockTime.plusSeconds(timeoutInSeconds));
    }

    @Override
    public String toString() {
        return "SeatLock{" +
                "seat=" + seat.getId() +
                ", show=" + show.getId() +
                ", user=" + user.getName() +
                ", isExpired=" + isExpired() +
                '}';
    }
}
