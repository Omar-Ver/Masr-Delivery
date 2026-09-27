package com.masrdelivery.analytics;

import com.masrdelivery.domain.MenuItem;
import com.masrdelivery.domain.Order;
import com.masrdelivery.domain.Rider;

import java.math.BigDecimal;
import java.util.List;

public class AnalyticsDTOs {

    public static class RiderPerformanceReport {
        private final Rider rider;
        private final int completedDeliveries;
        private final double averageDurationMinutes;

        public RiderPerformanceReport(Rider rider, int completedDeliveries, double averageDurationMinutes) {
            this.rider = rider;
            this.completedDeliveries = completedDeliveries;
            this.averageDurationMinutes = averageDurationMinutes;
        }

        public Rider getRider() { return rider; }
        public int getCompletedDeliveries() { return completedDeliveries; }
        public double getAverageDurationMinutes() { return averageDurationMinutes; }
    }

    public static class CustomerOrderHistoryReport {
        private final List<Order> orders;
        private final BigDecimal totalSpent;

        public CustomerOrderHistoryReport(List<Order> orders, BigDecimal totalSpent) {
            this.orders = orders;
            this.totalSpent = totalSpent;
        }

        public List<Order> getOrders() { return orders; }
        public BigDecimal getTotalSpent() { return totalSpent; }
    }

    public static class MenuItemFrequency {
        private final MenuItem menuItem;
        private final BigDecimal totalQuantityOrdered;

        public MenuItemFrequency(MenuItem menuItem, BigDecimal totalQuantityOrdered) {
            this.menuItem = menuItem;
            this.totalQuantityOrdered = totalQuantityOrdered;
        }

        public MenuItem getMenuItem() { return menuItem; }
        public BigDecimal getTotalQuantityOrdered() { return totalQuantityOrdered; }
    }
}