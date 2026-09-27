package com.masrdelivery.domain;

import com.masrdelivery.exception.PlatformException;

import java.math.BigDecimal;
import java.util.List;

public class MenuItemFactory {

    public static MenuItem createStandard(
            String id,
            String name,
            BigDecimal price,
            String category,
            int prepTimeMinutes,
            boolean available
    ) {
        return new StandardMenuItem(id, name, price, category, prepTimeMinutes, available); //[cite: 1]
    }

    public static MenuItem createCombo(
            String id,
            String name,
            List<MenuItem> bundledItems,
            BigDecimal discountRate,
            String category,
            int prepTimeMinutes,
            boolean available
    ) {
        return new ComboMenuItem(id, name, bundledItems, discountRate, category, prepTimeMinutes, available); //[cite: 1]
    }

    public static MenuItem createWeighted(
            String id,
            String name,
            BigDecimal pricePerKg,
            String category,
            int prepTimeMinutes,
            boolean available
    ) {
        return new WeightedMenuItem(id, name, pricePerKg, category, prepTimeMinutes, available); //[cite: 1]
    }

    /**
     * Centralizes menu item creation from a data record or file entry,
     * fulfilling the requirement that type-switching logic is never scattered[cite: 1].
     */
    public static MenuItem createFromRecord(
            String type,
            String id,
            String name,
            BigDecimal priceOrRate,
            String category,
            int prepTimeMinutes,
            boolean available,
            List<MenuItem> bundledItems
    ) {
        if (type == null || type.trim().isEmpty()) {
            throw new PlatformException("Menu item type cannot be null or empty.");
        }

        switch (type.trim().toUpperCase()) {
            case "STANDARD":
                return createStandard(id, name, priceOrRate, category, prepTimeMinutes, available); //[cite: 1]
            case "WEIGHTED":
                return createWeighted(id, name, priceOrRate, category, prepTimeMinutes, available); //[cite: 1]
            case "COMBO":
                return createCombo(id, name, bundledItems, priceOrRate, category, prepTimeMinutes, available); //[cite: 1]
            default:
                throw new PlatformException("Unknown menu item type: " + type);
        }
    }
}