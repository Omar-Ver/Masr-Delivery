package com.masrdelivery.pricing;

import com.masrdelivery.domain.Customer;
import com.masrdelivery.exception.PlatformException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public abstract class BasePromotion implements Promotion {
    private final String code;
    private final BigDecimal minSubtotal; //[cite: 1]
    private final LocalDate expiryDate; //[cite: 1]
    private final String restrictedDistrict; //[cite: 1]
    private final boolean firstTimeCustomerOnly; //[cite: 1]

    public BasePromotion(String code, BigDecimal minSubtotal, LocalDate expiryDate, String restrictedDistrict, boolean firstTimeOnly) {
        this.code = code.toUpperCase(); //[cite: 1]
        this.minSubtotal = minSubtotal != null ? minSubtotal : BigDecimal.ZERO;
        this.expiryDate = expiryDate;
        this.restrictedDistrict = restrictedDistrict;
        this.firstTimeCustomerOnly = firstTimeOnly;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void validateConditions(Customer customer, BigDecimal subtotal, String deliveryDistrict) {
        if (expiryDate != null && LocalDate.now().isAfter(expiryDate)) {
            throw new PlatformException("Promotion " + code + " has expired."); //[cite: 1]
        }
        if (subtotal.compareTo(minSubtotal) < 0) {
            throw new PlatformException("Promotion " + code + " requires a minimum subtotal of " + minSubtotal + " EGP."); //[cite: 1]
        }
        if (restrictedDistrict != null && !restrictedDistrict.equalsIgnoreCase(deliveryDistrict)) {
            throw new PlatformException("Promotion " + code + " is restricted to " + restrictedDistrict + "."); //[cite: 1]
        }
        if (firstTimeCustomerOnly && customer.getLoyaltyTier() != com.masrdelivery.domain.LoyaltyTier.BRONZE) {
            throw new PlatformException("Promotion " + code + " is reserved for first-time customers only."); //[cite: 1]
        }
    }

    @Override
    public boolean isFreeDelivery() {
        return false;
    }
}