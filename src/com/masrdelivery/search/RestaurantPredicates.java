package com.masrdelivery.search;

import com.masrdelivery.domain.Restaurant;

import java.math.BigDecimal;
import java.util.function.Predicate;

public class RestaurantPredicates {

    /**
     * Matches open restaurants only.
     */
    public static Predicate<Restaurant> isOpen() {
        return Restaurant::isOpen;
    }

    /**
     * Filters by restaurant district (case-insensitive).
     */
    public static Predicate<Restaurant> byDistrict(String district) {
        if (district == null || district.trim().isEmpty()) {
            return r -> true; // No-op filter
        }
        return r -> r.getDistrict().equalsIgnoreCase(district.trim());
    }

    /**
     * Matches if the restaurant offers the specified cuisine category[cite: 1].
     */
    public static Predicate<Restaurant> byCuisine(String cuisine) {
        if (cuisine == null || cuisine.trim().isEmpty()) {
            return r -> true;
        }
        return r -> r.getCuisines().stream()
                .anyMatch(c -> c.equalsIgnoreCase(cuisine.trim()));
    }

    /**
     * Filters restaurants with an average rating greater than or equal to minRating[cite: 1].
     */
    public static Predicate<Restaurant> byMinRating(double minRating) {
        return r -> r.getRating() >= minRating;
    }

    /**
     * Matches restaurants offering at least one menu item at or below the price ceiling[cite: 1].
     */
    public static Predicate<Restaurant> byPriceCeiling(BigDecimal maxPrice) {
        if (maxPrice == null || maxPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return r -> true;
        }
        return r -> r.getMenu().stream()
                .anyMatch(item -> item.calculatePrice(BigDecimal.ONE).compareTo(maxPrice) <= 0);
    }

    /**
     * Free-text search matching restaurant name or any cuisine category[cite: 1].
     */
    public static Predicate<Restaurant> byFreeText(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return r -> true;
        }
        String term = keyword.trim().toLowerCase();
        return r -> r.getName().toLowerCase().contains(term)
                || r.getCuisines().stream().anyMatch(c -> c.toLowerCase().contains(term));
    }
}