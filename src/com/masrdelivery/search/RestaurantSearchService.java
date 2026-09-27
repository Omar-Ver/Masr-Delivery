package com.masrdelivery.search;

import com.masrdelivery.domain.Restaurant;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RestaurantSearchService {

    /**
     * Filters a collection of restaurants using any composed predicate
     * without modifying the underlying search logic when new criteria are added[cite: 1].
     * Results are sorted by rating descending, ties broken alphabetically[cite: 1].
     */
    public static List<Restaurant> search(Collection<Restaurant> restaurants, Predicate<Restaurant> criteria) {
        Predicate<Restaurant> filterCondition = criteria != null ? criteria : r -> true;

        return restaurants.stream()
                .filter(filterCondition)
                .sorted() // Uses Restaurant.compareTo: rating descending, name ascending[cite: 1]
                .collect(Collectors.toUnmodifiableList());
    }
}