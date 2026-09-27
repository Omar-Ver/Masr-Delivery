package com.masrdelivery.domain;

import com.masrdelivery.exception.PlatformException;
import java.math.BigDecimal;
import java.util.Objects;

public abstract class MenuItem {
    private final String id; //[cite: 1]
    private final String name; //[cite: 1]
    private final String category; //[cite: 1]
    private final int prepTimeMinutes; //[cite: 1]
    private boolean available; //[cite: 1]

    public MenuItem(String id, String name, String category, int prepTimeMinutes, boolean available) {
        if (id == null || id.trim().isEmpty()) {
            throw new PlatformException("Menu item ID cannot be null or empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new PlatformException("Menu item name cannot be null or empty.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new PlatformException("Category cannot be null or empty.");
        }
        if (prepTimeMinutes <= 0) {
            throw new PlatformException("Preparation time must be greater than zero.");
        }
        this.id = id;
        this.name = name;
        this.category = category;
        this.prepTimeMinutes = prepTimeMinutes;
        this.available = available;
    }

    public abstract BigDecimal calculatePrice(BigDecimal quantity); //[cite: 1]

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    // Enforce identity by ID within the restaurant[cite: 1]
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(id, menuItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}