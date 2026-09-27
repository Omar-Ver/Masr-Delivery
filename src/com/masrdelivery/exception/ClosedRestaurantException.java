package com.masrdelivery.exception;

public class ClosedRestaurantException extends PlatformException {
    public ClosedRestaurantException(String restaurantName) {
        super("Order cannot be placed: "+ restaurantName + " is currently closed.");
    }
}
