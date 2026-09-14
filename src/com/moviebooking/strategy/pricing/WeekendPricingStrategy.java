package com.moviebooking.strategy.pricing;

import com.moviebooking.model.Seat;
import com.moviebooking.model.Show;

import java.time.DayOfWeek;

/**
 * Dynamic pricing strategy that applies a 20% premium on weekend shows (Saturday and Sunday).
 */
public class WeekendPricingStrategy implements PricingStrategy {

    private final StandardPricingStrategy basePricing = new StandardPricingStrategy();
    private static final double SURGE_MULTIPLIER = 1.20; // 20% surge

    @Override
    public double calculatePrice(Show show, Seat seat) {
        double base = basePricing.calculatePrice(show, seat);
        DayOfWeek day = show.getStartTime().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return Math.round(base * SURGE_MULTIPLIER * 100.0) / 100.0;
        }
        return base;
    }
}
