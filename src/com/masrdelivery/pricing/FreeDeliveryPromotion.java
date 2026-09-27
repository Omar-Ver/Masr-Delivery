package com.masrdelivery.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FreeDeliveryPromotion extends BasePromotion {
    public FreeDeliveryPromotion(String code, BigDecimal minSubtotal, LocalDate expiryDate, String district, boolean firstTimeOnly) {
        super(code, minSubtotal, expiryDate, district, firstTimeOnly);
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal, BigDecimal deliveryFee) {
        return deliveryFee; //[cite: 1]
    }

    @Override
    public boolean isFreeDelivery() {
        return true; //[cite: 1]
    }
}