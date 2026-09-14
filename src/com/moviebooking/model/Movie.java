package com.moviebooking.model;

/**
 * Represents a Movie being exhibited across cinema halls.
 */
public class Movie {
    private final String id;
    private final String title;
    private final int durationInMinutes;
    private final String genre;
    private final String language;

    public Movie(String id, String title, int durationInMinutes, String genre, String language) {
        this.id = id;
        this.title = title;
        this.durationInMinutes = durationInMinutes;
        this.genre = genre;
        this.language = language;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public String getGenre() {
        return genre;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return title + " [" + genre + " | " + language + " | " + durationInMinutes + " mins]";
    }
}
