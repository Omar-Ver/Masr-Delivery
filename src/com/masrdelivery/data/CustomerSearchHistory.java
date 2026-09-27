package com.masrdelivery.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class CustomerSearchHistory {
    private static final int MAX_HISTORY = 5; //
    private final Deque<String> searches = new ArrayDeque<>(MAX_HISTORY); //

    public synchronized void recordSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        if (searches.size() == MAX_HISTORY) {
            searches.removeLast(); // Evict oldest to cap history at five
        }
        searches.addFirst(query.trim()); // Place newest at the front
    }

    public synchronized List<String> getRecentSearches() {
        // Return newest first in an unmodifiable view
        return Collections.unmodifiableList(new ArrayList<>(searches));
    }
}