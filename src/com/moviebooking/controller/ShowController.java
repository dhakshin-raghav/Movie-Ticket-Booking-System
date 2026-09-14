package com.moviebooking.controller;

import com.moviebooking.model.CinemaHall;
import com.moviebooking.model.Movie;
import com.moviebooking.model.Screen;
import com.moviebooking.model.Seat;
import com.moviebooking.model.Show;
import com.moviebooking.service.ShowService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller exposing show management and seat availability endpoints.
 */
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    public Show createShow(String id, Movie movie, CinemaHall cinemaHall, Screen screen, 
                           LocalDateTime startTime, LocalDateTime endTime) {
        Show show = new Show(id, movie, cinemaHall, screen, startTime, endTime);
        showService.addShow(show);
        return show;
    }

    public Optional<Show> getShowById(String id) {
        return showService.getShowById(id);
    }

    public List<Show> getShowsForMovie(String movieId) {
        return showService.getShowsForMovie(movieId);
    }

    public List<Seat> getAvailableSeats(Show show) {
        return showService.getAvailableSeats(show);
    }
}
