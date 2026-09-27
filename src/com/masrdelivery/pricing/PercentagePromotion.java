package com.masrdelivery.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class PercentagePromotion extends BasePromotion {
    private final BigDecimal percentage; // e.g., 0.20 for 20%[cite: 1]
    private final BigDecimal maxCap; //[cite: 1]

    public PercentagePromotion(String code, BigDecimal percentage, BigDecimal maxCap, BigDecimal minSubtotal, LocalDate expiryDate, String district, boolean firstTimeOnly) {
        super(code, minSubtotal, expiryDate, district, firstTimeOnly);
        this.percentage = percentage;
        this.maxCap = maxCap;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal, BigDecimal deliveryFee) {
        BigDecimal discount = subtotal.multiply(percentage).setScale(2, RoundingMode.HALF_UP); //[cite: 1]
        if (maxCap != null && discount.compareTo(maxCap) > 0) {
            return maxCap.setScale(2, RoundingMode.HALF_UP); //[cite: 1]
        }
        return discount;
    }
}