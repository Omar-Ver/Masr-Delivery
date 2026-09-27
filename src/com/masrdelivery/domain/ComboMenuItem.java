package com.masrdelivery.domain;

import com.masrdelivery.exception.PlatformException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComboMenuItem extends MenuItem {
    private final List<MenuItem> bundledItems;
    private final BigDecimal discountRate; // e.g., new BigDecimal("0.15") for 15% off[cite: 1]

    public ComboMenuItem(
            String id,
            String name,
            List<MenuItem> bundledItems,
            BigDecimal discountRate,
            String category,
            int prepTimeMinutes,
            boolean available
    ) {
        super(id, name, category, prepTimeMinutes, available);

        if (bundledItems == null || bundledItems.isEmpty()) {
            throw new PlatformException("A combo must bundle at least one item."); //[cite: 1]
        }
        if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) < 0 || discountRate.compareTo(BigDecimal.ONE) >= 0) {
            throw new PlatformException("Discount rate must be between 0.0 and 1.0.");
        }

        // Defensive copy to prevent external mutation[cite: 1]
        this.bundledItems = new ArrayList<>(bundledItems);
        this.discountRate = discountRate;
    }

    /**
     * Calculates bundle price: sum of parts minus discount[cite: 1].
     */
    public BigDecimal getBundlePrice() {
        BigDecimal sum = bundledItems.stream()
                .map(item -> item.calculatePrice(BigDecimal.ONE))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = sum.multiply(discountRate);
        return sum.subtract(discount).setScale(2, RoundingMode.HALF_UP); //[cite: 1]
    }

    @Override
    public BigDecimal calculatePrice(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlatformException("Quantity must be greater than zero."); //[cite: 1]
        }
        return getBundlePrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP); //[cite: 1]
    }

    public List<MenuItem> getBundledItems() {
        return Collections.unmodifiableList(bundledItems); //[cite: 1]
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }
}