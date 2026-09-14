package com.moviebooking.repository;

import com.moviebooking.model.Show;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory repository for storing and querying Shows.
 */
public class ShowRepository {
    private final Map<String, Show> shows = new ConcurrentHashMap<>();

    public void save(Show show) {
        shows.put(show.getId(), show);
    }

    public Optional<Show> findById(String id) {
        return Optional.ofNullable(shows.get(id));
    }

    public List<Show> findAll() {
        return new ArrayList<>(shows.values());
    }

    public List<Show> findByMovieId(String movieId) {
        return shows.values().stream()
                .filter(show -> show.getMovie().getId().equals(movieId))
                .collect(Collectors.toList());
    }
}
