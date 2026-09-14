package com.moviebooking.data;

import com.moviebooking.controller.MovieController;
import com.moviebooking.controller.ShowController;
import com.moviebooking.model.*;

import java.time.LocalDateTime;

/**
 * Initializes realistic sample data across multiple cities, theatres, screens, movies, and shows.
 */
public class DataInitializer {

    public static void initialize(MovieController movieController, ShowController showController) {
        // 1. Movies
        movieController.addMovie("MOV-1", "Interstellar", 169, "Sci-Fi / Adventure", "English");
        movieController.addMovie("MOV-2", "Inception", 148, "Sci-Fi / Thriller", "English");
        movieController.addMovie("MOV-3", "The Dark Knight", 152, "Action / Crime", "English");

        Movie interstellar = movieController.getMovieById("MOV-1").orElseThrow();
        Movie inception = movieController.getMovieById("MOV-2").orElseThrow();
        Movie darkKnight = movieController.getMovieById("MOV-3").orElseThrow();

        // 2. Theatres & Screens
        // Bangalore Theatres
        CinemaHall pvrBangalore = new CinemaHall("TH-BLR-1", "PVR Cinemas, Forum Mall", City.BANGALORE);
        Screen blrScreen1 = createStandardScreen("SCR-BLR-1", "Audi 1 (IMAX)");
        pvrBangalore.addScreen(blrScreen1);

        CinemaHall inoxBangalore = new CinemaHall("TH-BLR-2", "INOX, Phoenix Marketcity", City.BANGALORE);
        Screen blrScreen2 = createStandardScreen("SCR-BLR-2", "Screen 2 (Dolby Atmos)");
        inoxBangalore.addScreen(blrScreen2);

        // Mumbai Theatres
        CinemaHall pvrMumbai = new CinemaHall("TH-BOM-1", "PVR ICON, Lower Parel", City.MUMBAI);
        Screen bomScreen1 = createStandardScreen("SCR-BOM-1", "Gold Screen 1");
        pvrMumbai.addScreen(bomScreen1);

        // Delhi Theatres
        CinemaHall cinepolisDelhi = new CinemaHall("TH-DEL-1", "Cinepolis VIP, DLF Avenue", City.DELHI);
        Screen delScreen1 = createStandardScreen("SCR-DEL-1", "Screen 1");
        cinepolisDelhi.addScreen(delScreen1);

        // 3. Shows
        LocalDateTime todayEvening = LocalDateTime.now().withHour(18).withMinute(30).withSecond(0).withNano(0);
        LocalDateTime todayNight = LocalDateTime.now().withHour(21).withMinute(45).withSecond(0).withNano(0);
        LocalDateTime weekendShow = LocalDateTime.now().plusDays(2).withHour(19).withMinute(0).withSecond(0).withNano(0);

        // Bangalore Shows
        showController.createShow("SHOW-BLR-01", interstellar, pvrBangalore, blrScreen1, todayEvening, todayEvening.plusMinutes(169));
        showController.createShow("SHOW-BLR-02", inception, pvrBangalore, blrScreen1, todayNight, todayNight.plusMinutes(148));
        showController.createShow("SHOW-BLR-03", darkKnight, inoxBangalore, blrScreen2, todayEvening, todayEvening.plusMinutes(152));
        showController.createShow("SHOW-BLR-04", interstellar, inoxBangalore, blrScreen2, weekendShow, weekendShow.plusMinutes(169));

        // Mumbai Shows
        showController.createShow("SHOW-BOM-01", interstellar, pvrMumbai, bomScreen1, todayEvening, todayEvening.plusMinutes(169));
        showController.createShow("SHOW-BOM-02", darkKnight, pvrMumbai, bomScreen1, todayNight, todayNight.plusMinutes(152));

        // Delhi Shows
        showController.createShow("SHOW-DEL-01", inception, cinepolisDelhi, delScreen1, todayEvening, todayEvening.plusMinutes(148));
        showController.createShow("SHOW-DEL-02", interstellar, cinepolisDelhi, delScreen1, todayNight, todayNight.plusMinutes(169));
    }

    private static Screen createStandardScreen(String screenId, String screenName) {
        Screen screen = new Screen(screenId, screenName);
        // Row A: Silver (A1 - A5)
        for (int i = 1; i <= 5; i++) {
            screen.addSeat(new Seat("A" + i, "A", i, SeatType.SILVER));
        }
        // Row B: Gold (B1 - B5)
        for (int i = 1; i <= 5; i++) {
            screen.addSeat(new Seat("B" + i, "B", i, SeatType.GOLD));
        }
        // Row C: Platinum (C1 - C5)
        for (int i = 1; i <= 5; i++) {
            screen.addSeat(new Seat("C" + i, "C", i, SeatType.PLATINUM));
        }
        return screen;
    }
}
