package com.moviebooking.repository;

import com.moviebooking.model.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory repository for storing and querying Movies.
 */
public class MovieRepository {
    private final Map<String, Movie> movies = new ConcurrentHashMap<>();

    public void save(Movie movie) {
        movies.put(movie.getId(), movie);
    }

    public Optional<Movie> findById(String id) {
        return Optional.ofNullable(movies.get(id));
    }

    public List<Movie> findAll() {
        return new ArrayList<>(movies.values());
    }
}
