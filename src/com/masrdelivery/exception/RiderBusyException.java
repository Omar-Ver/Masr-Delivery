package com.masrdelivery.exception;

public class RiderBusyException extends PlatformException {
    public RiderBusyException(String riderId) {
        super("Rider " + riderId + " is already assigned to an active order."); //
    }
}