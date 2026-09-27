package com.masrdelivery.domain;


import com.masrdelivery.exception.PlatformException;

import java.math.BigDecimal;

public class StandardMenuItem extends MenuItem {
    private final BigDecimal price;

    public StandardMenuItem(String id, String name,BigDecimal price, String category, int prepTimeMinutes, boolean available) {
        super(id, name, category, prepTimeMinutes, available);
        if (price.compareTo(BigDecimal.ZERO)<=0){
            throw new PlatformException("Price must be greater than zero.");
        }
        this.price = price;
    }

    @Override
    public BigDecimal calculatePrice(BigDecimal quantity) {
        return price.multiply(quantity);
    }

    public BigDecimal getPrice() {
        return price;
    }
}
