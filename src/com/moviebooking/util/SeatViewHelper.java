package com.moviebooking.util;

import com.moviebooking.model.Seat;
import com.moviebooking.model.SeatStatus;
import com.moviebooking.model.Show;
import com.moviebooking.service.SeatLockService;
import com.moviebooking.strategy.pricing.PricingStrategy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility helper to display visual ASCII seat layouts in the console.
 */
public class SeatViewHelper {

    public static void printSeatLayout(Show show, SeatLockService seatLockService, PricingStrategy pricingStrategy) {
        System.out.println("\n═════════════════════════════════════════════════════════════════════════════");
        System.out.println("                            🎬  SCREEN THIS WAY  🎬                          ");
        System.out.println("═════════════════════════════════════════════════════════════════════════════\n");

        List<Seat> allSeats = show.getScreen().getSeats();
        // Group by row (A, B, C...)
        Map<String, List<Seat>> seatsByRow = allSeats.stream()
                .collect(Collectors.groupingBy(Seat::getRow, TreeMap::new, Collectors.toList()));

        for (Map.Entry<String, List<Seat>> entry : seatsByRow.entrySet()) {
            String row = entry.getKey();
            List<Seat> rowSeats = entry.getValue();
            rowSeats.sort(Comparator.comparingInt(Seat::getSeatNumber));

            Seat firstSeat = rowSeats.get(0);
            double price = pricingStrategy.calculatePrice(show, firstSeat);
            String rowLabel = String.format(" Row %s (%-8s - $%.0f):  ", row, firstSeat.getSeatType(), price);
            System.out.print(rowLabel);

            for (Seat seat : rowSeats) {
                SeatStatus status = seatLockService.getSeatStatus(show, seat);
                switch (status) {
                    case AVAILABLE:
                        System.out.print("[🟢 " + seat.getId() + "] ");
                        break;
                    case LOCKED:
                        System.out.print("[⏳ " + seat.getId() + "] ");
                        break;
                    case BOOKED:
                        System.out.print("[❌ " + seat.getId() + "] ");
                        break;
                }
            }
            System.out.println();
        }

        System.out.println("\n─────────────────────────────────────────────────────────────────────────────");
        System.out.println(" Legend:  [🟢 Available]   [⏳ Locked / Checking Out]   [❌ Booked]");
        System.out.println("─────────────────────────────────────────────────────────────────────────────\n");
    }
}
