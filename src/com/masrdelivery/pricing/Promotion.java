package com.masrdelivery.pricing;

import com.masrdelivery.domain.Customer;
import com.masrdelivery.exception.PlatformException;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface Promotion {
    String getCode(); //[cite: 1]
    void validateConditions(Customer customer, BigDecimal subtotal, String deliveryDistrict); //[cite: 1]
    BigDecimal calculateDiscount(BigDecimal subtotal, BigDecimal deliveryFee); //[cite: 1]
    boolean isFreeDelivery(); //[cite: 1]
}