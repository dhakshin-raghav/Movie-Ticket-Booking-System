package com.moviebooking.strategy.pricing;

import com.moviebooking.model.Seat;
import com.moviebooking.model.Show;

/**
 * Strategy pattern interface for dynamic seat pricing.
 * Allows switching between standard pricing, weekend pricing, rush-hour, or discount pricing.
 */
public interface PricingStrategy {
    double calculatePrice(Show show, Seat seat);
}
