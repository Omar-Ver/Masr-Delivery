package com.masrdelivery.domain;

import com.masrdelivery.exception.PlatformException;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Restaurant implements Comparable<Restaurant> {
    private final String id;
    private final String name;
    private final String district;
    private final Set<String> cuisines;
    private double rating;
    private boolean open;
    // LinkedHashSet guarantees menu items display in insertion order on every run
    private final Set<MenuItem> menu = new LinkedHashSet<>();
    private int completedOrdersCount;

    public Restaurant(String id, String name, String district, Set<String> cuisines, double rating, boolean open) {
        if (id == null || id.trim().isEmpty()) {
            throw new PlatformException("Restaurant ID cannot be null or empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new PlatformException("Restaurant name cannot be null or empty.");
        }
        if (district == null || district.trim().isEmpty()) {
            throw new PlatformException("District cannot be null or empty.");
        }
        if (cuisines == null || cuisines.isEmpty()) {
            throw new PlatformException("A restaurant must have at least one cuisine category.");
        }
        validateRating(rating);

        this.id = id;
        this.name = name;
        this.district = district;
        this.cuisines = new HashSet<>(cuisines);
        this.rating = rating;
        this.open = open;
        this.completedOrdersCount = 0;
    }

    private void validateRating(double rating) {
        if (rating < 0.0 || rating > 5.0) {
            throw new PlatformException("Rating must be between 0.0 and 5.0 inclusive.");
        }
    }

    public void addMenuItem(MenuItem item) {
        if (item == null) {
            throw new PlatformException("Cannot add a null menu item.");
        }
        // Identifiers are unique within the restaurant
        boolean duplicate = menu.stream().anyMatch(m -> m.getId().equalsIgnoreCase(item.getId()));
        if (duplicate) {
            throw new PlatformException("Menu item with ID " + item.getId() + " already exists in this restaurant.");
        }
        menu.add(item);
    }

    public void removeMenuItem(String menuItemId) {
        menu.removeIf(item -> item.getId().equalsIgnoreCase(menuItemId));
    }

    public void setRating(double rating) {
        validateRating(rating);
        this.rating = rating;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void incrementCompletedOrders() {
        this.completedOrdersCount++;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDistrict() { return district; }
    public double getRating() { return rating; }
    public boolean isOpen() { return open; }
    public int getCompletedOrdersCount() { return completedOrdersCount; }

    // Enforce unmodifiable exposed collections
    public Set<String> getCuisines() {
        return Collections.unmodifiableSet(cuisines);
    }

    public Set<MenuItem> getMenu() {
        return Collections.unmodifiableSet(menu);
    }

    /**
     * Orders restaurants sorted by rating descending,
     * breaking ties alphabetically by name.
     */
    @Override
    public int compareTo(Restaurant other) {
        int ratingComparison = Double.compare(other.rating, this.rating);
        if (ratingComparison != 0) {
            return ratingComparison;
        }
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}