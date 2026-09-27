package com.masrdelivery.domain;

import com.masrdelivery.exception.PlatformException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class WeightedMenuItem extends MenuItem {
    private final BigDecimal pricePerKg;

    public WeightedMenuItem(String id, String name, BigDecimal pricePerKg, String category, int prepTimeMinutes, boolean available) {
        super(id, name, category, prepTimeMinutes, available);
        if (pricePerKg == null || pricePerKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlatformException("Price per kilogram must be greater than zero."); //[cite: 1]
        }
        this.pricePerKg = pricePerKg;
    }

    @Override
    public BigDecimal calculatePrice(BigDecimal quantityInKg) {
        if (quantityInKg == null || quantityInKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlatformException("Weight ordered must be greater than zero."); //[cite: 1]
        }
        return pricePerKg.multiply(quantityInKg).setScale(2, RoundingMode.HALF_UP); //[cite: 1]
    }

    public BigDecimal getPricePerKg() {
        return pricePerKg;
    }
}