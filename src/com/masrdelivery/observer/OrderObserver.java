package com.masrdelivery.observer;

import com.masrdelivery.domain.Order;
import com.masrdelivery.domain.OrderStatus;

public interface OrderObserver {
    void update(Order order, OrderStatus oldStatus, OrderStatus newStatus);
}