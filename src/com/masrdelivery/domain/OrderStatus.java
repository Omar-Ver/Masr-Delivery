package com.masrdelivery.domain;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PLACED,
    ACCEPTED,
    PREPARING,
    READY,
    ASSIGNED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;


    public boolean canTransitionTo(OrderStatus target) {
        if (target == null || this == target) {
            return false;
        }

        // CANCELLED and DELIVERED are terminal states
        if (this == CANCELLED || this == DELIVERED) {
            return false;
        }

        // Cancellation is permitted at any stage before OUT_FOR_DELIVERY
        if (target == CANCELLED) {
            return this != OUT_FOR_DELIVERY;

        }

        // Sequential lifecycle progression
        switch (this) {
            case PLACED:
                return target == ACCEPTED;
            case ACCEPTED:
                return target == PREPARING;
            case PREPARING:
                return target == READY;
            case READY:
                return target == ASSIGNED;
            case ASSIGNED:
                return target == OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY:
                return target == DELIVERED;
            default:
                return false;
        }
    }
}