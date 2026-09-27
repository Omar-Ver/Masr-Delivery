package com.masrdelivery.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FixedAmountPromotion extends BasePromotion {
    private final BigDecimal fixedDiscount; //[cite: 1]

    public FixedAmountPromotion(String code, BigDecimal fixedDiscount, BigDecimal minSubtotal, LocalDate expiryDate, String district, boolean firstTimeOnly) {
        super(code, minSubtotal, expiryDate, district, firstTimeOnly);
        this.fixedDiscount = fixedDiscount;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal, BigDecimal deliveryFee) {
        return fixedDiscount.min(subtotal); //[cite: 1]
    }
}