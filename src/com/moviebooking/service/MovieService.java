package com.moviebooking.service;

import com.moviebooking.model.Movie;
import com.moviebooking.repository.MovieRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for movie management.
 */
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public void addMovie(Movie movie) {
        movieRepository.save(movie);
    }

    public Optional<Movie> getMovieById(String id) {
        return movieRepository.findById(id);
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }
}
