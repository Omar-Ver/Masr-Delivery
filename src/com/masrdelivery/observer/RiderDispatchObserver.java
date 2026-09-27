package com.masrdelivery.observer;

import com.masrdelivery.domain.Order;
import com.masrdelivery.domain.OrderStatus;

public class RiderDispatchObserver implements OrderObserver {
    @Override
    public void update(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        // Only react when the order is ready for pickup
        if (newStatus == OrderStatus.READY) {
            System.out.printf("\n[DISPATCH SYSTEM ALERT]: Order %s is READY at %s. Notifying available riders in %s.%n",
                    order.getId(), order.getRestaurant().getName(), order.getRestaurant().getDistrict());
        }
    }
}