package com.masrdelivery.domain;

import com.masrdelivery.exception.PlatformException;
import java.math.BigDecimal;
import java.util.Objects;

public class OrderLineItem {
    private final MenuItem menuItem;
    private final BigDecimal quantity;

    public OrderLineItem(MenuItem menuItem, BigDecimal quantity) {
        if (menuItem == null) {
            throw new PlatformException("Line item must reference a valid menu item.");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlatformException("Item quantity or weight must be greater than zero.");
        }
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public BigDecimal calculateSubtotal() {
        return menuItem.calculatePrice(quantity);
    }

    public MenuItem getMenuItem() { return menuItem; }
    public BigDecimal getQuantity() { return quantity; }
}