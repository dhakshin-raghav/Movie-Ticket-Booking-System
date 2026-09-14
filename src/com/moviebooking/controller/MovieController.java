package com.moviebooking.controller;

import com.moviebooking.model.Movie;
import com.moviebooking.service.MovieService;

import java.util.List;
import java.util.Optional;

/**
 * Controller exposing movie catalog endpoints.
 */
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    public void addMovie(String id, String title, int durationMinutes, String genre, String language) {
        Movie movie = new Movie(id, title, durationMinutes, genre, language);
        movieService.addMovie(movie);
    }

    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }

    public Optional<Movie> getMovieById(String id) {
        return movieService.getMovieById(id);
    }
}
