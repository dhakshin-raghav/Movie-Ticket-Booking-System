package com.moviebooking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Cinema Hall / Theatre multiplex located in a city.
 */
public class CinemaHall {
    private final String id;
    private final String name;
    private final City city;
    private final List<Screen> screens;

    public CinemaHall(String id, String name, City city) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.screens = new ArrayList<>();
    }

    public void addScreen(Screen screen) {
        this.screens.add(screen);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public City getCity() {
        return city;
    }

    public List<Screen> getScreens() {
        return Collections.unmodifiableList(screens);
    }

    @Override
    public String toString() {
        return name + " (" + city + ")";
    }
}
