package com.moviebooking.strategy.pricing;

import com.moviebooking.model.Seat;
import com.moviebooking.model.Show;

/**
 * Standard fixed pricing strategy based on seat tier (SILVER, GOLD, PLATINUM).
 */
public class StandardPricingStrategy implements PricingStrategy {

    private static final double SILVER_PRICE = 150.0;
    private static final double GOLD_PRICE = 250.0;
    private static final double PLATINUM_PRICE = 400.0;

    @Override
    public double calculatePrice(Show show, Seat seat) {
        switch (seat.getSeatType()) {
            case PLATINUM:
                return PLATINUM_PRICE;
            case GOLD:
                return GOLD_PRICE;
            case SILVER:
            default:
                return SILVER_PRICE;
        }
    }
}
