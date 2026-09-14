package com.moviebooking.service;

import com.moviebooking.model.Seat;
import com.moviebooking.model.Show;
import com.moviebooking.repository.ShowRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for show scheduling and seat availability querying.
 */
public class ShowService {
    private final ShowRepository showRepository;
    private final SeatLockService seatLockService;

    public ShowService(ShowRepository showRepository, SeatLockService seatLockService) {
        this.showRepository = showRepository;
        this.seatLockService = seatLockService;
    }

    public void addShow(Show show) {
        showRepository.save(show);
    }

    public Optional<Show> getShowById(String id) {
        return showRepository.findById(id);
    }

    public List<Show> getAllShows() {
        return showRepository.findAll();
    }

    public List<Show> getShowsForMovie(String movieId) {
        return showRepository.findByMovieId(movieId);
    }

    public List<Seat> getAvailableSeats(Show show) {
        return seatLockService.getAvailableSeats(show);
    }
}
