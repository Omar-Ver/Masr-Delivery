package com.masrdelivery.observer;

import com.masrdelivery.domain.Order;
import com.masrdelivery.domain.OrderStatus;

public class CustomerNotificationObserver implements OrderObserver {
    @Override
    public void update(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        // We don't notify the customer when they initially place the order themselves
        if (oldStatus == OrderStatus.PLACED && newStatus == OrderStatus.PLACED) {
            return;
        }

        System.out.printf("\n[PUSH NOTIFICATION to %s]: Your order %s is now %s!%n",
                order.getCustomer().getName(), order.getId(), newStatus);
    }
}