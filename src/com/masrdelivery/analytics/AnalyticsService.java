package com.masrdelivery.analytics;

import com.masrdelivery.analytics.AnalyticsDTOs.CustomerOrderHistoryReport;
import com.masrdelivery.analytics.AnalyticsDTOs.MenuItemFrequency;
import com.masrdelivery.analytics.AnalyticsDTOs.RiderPerformanceReport;
import com.masrdelivery.data.PlatformDataStore;
import com.masrdelivery.domain.Customer;
import com.masrdelivery.domain.Order;
import com.masrdelivery.domain.OrderLineItem;
import com.masrdelivery.domain.OrderStatus;
import com.masrdelivery.domain.Restaurant;
import com.masrdelivery.domain.Rider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnalyticsService {
    private final PlatformDataStore dataStore;

    public AnalyticsService(PlatformDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Report 1: Total revenue for a given date range[cite: 1].
     */
    public BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        return dataStore.getAllOrders().values().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .filter(order -> {
                    LocalDate orderDate = order.getPlacedAt().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .map(Order::getFinalTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Report 2: The top five restaurants by revenue, for a given month[cite: 1].
     */
    public List<Map.Entry<Restaurant, BigDecimal>> getTopFiveRestaurantsByRevenue(int year, Month month) {
        return dataStore.getAllOrders().values().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .filter(order -> order.getPlacedAt().getYear() == year && order.getPlacedAt().getMonth() == month)
                .collect(Collectors.groupingBy(
                        Order::getRestaurant,
                        Collectors.reducing(BigDecimal.ZERO, Order::getFinalTotal, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<Restaurant, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Report 3: Average order value per district[cite: 1].
     */
    public Map<String, BigDecimal> getAverageOrderValuePerDistrict() {
        return dataStore.getAllOrders().values().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(
                        order -> order.getDeliveryAddress().getDistrict(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                districtOrders -> {
                                    if (districtOrders.isEmpty()) return BigDecimal.ZERO;
                                    BigDecimal districtSum = districtOrders.stream()
                                            .map(Order::getFinalTotal)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return districtSum.divide(BigDecimal.valueOf(districtOrders.size()), 2, RoundingMode.HALF_UP);
                                }
                        )
                ));
    }

    /**
     * Report 4: Every restaurant with rating above 4.5 and at least twenty completed orders[cite: 1].
     */
    public List<Restaurant> getHighRatedPopularRestaurants() {
        return dataStore.getAllRestaurants().values().stream()
                .filter(restaurant -> restaurant.getRating() > 4.5 && restaurant.getCompletedOrdersCount() >= 20)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Report 5: Count of orders grouped by their current status[cite: 1].
     */
    public Map<OrderStatus, Long> getOrderCountByStatus() {
        return dataStore.getAllOrders().values().stream()
                .collect(Collectors.groupingBy(
                        Order::getStatus,
                        Collectors.counting()
                ));
    }

    /**
     * Report 6: Each rider's completed deliveries and average duration, sorted by deliveries descending[cite: 1].
     */
    public List<RiderPerformanceReport> getRiderPerformanceReport() {
        Map<Rider, List<Order>> ordersByRider = dataStore.getAllOrders().values().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED && order.getRider() != null && order.getDeliveredAt() != null)
                .collect(Collectors.groupingBy(Order::getRider));

        return dataStore.getAllRiders().values().stream()
                .map(rider -> {
                    List<Order> riderOrders = ordersByRider.getOrDefault(rider, Collections.emptyList());
                    double avgMinutes = riderOrders.stream()
                            .mapToLong(order -> Duration.between(order.getPlacedAt(), order.getDeliveredAt()).toMinutes())
                            .average()
                            .orElse(0.0);
                    return new RiderPerformanceReport(rider, rider.getCompletedDeliveriesCount(), avgMinutes);
                })
                .sorted(Comparator.comparingInt(RiderPerformanceReport::getCompletedDeliveries).reversed())
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Report 7: Most frequently ordered menu item across the platform.
     * Absence is represented with Optional.empty() rather than throwing an exception or returning null[cite: 1].
     */
    public Optional<MenuItemFrequency> getMostFrequentlyOrderedMenuItem() {
        return dataStore.getAllOrders().values().stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .flatMap(order -> order.getLineItems().stream())
                .collect(Collectors.groupingBy(
                        OrderLineItem::getMenuItem,
                        Collectors.reducing(BigDecimal.ZERO, OrderLineItem::getQuantity, BigDecimal::add)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> new MenuItemFrequency(entry.getKey(), entry.getValue()));
    }

    /**
     * Report 8: Customer full order history, newest first, with total lifetime spent[cite: 1].
     */
    public CustomerOrderHistoryReport getCustomerOrderHistory(String customerId) {
        List<Order> history = dataStore.getAllOrders().values().stream()
                .filter(order -> order.getCustomer().getId().equalsIgnoreCase(customerId))
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                .collect(Collectors.toUnmodifiableList());

        BigDecimal totalSpent = history.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getFinalTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CustomerOrderHistoryReport(history, totalSpent);
    }

    /**
     * Report 9: Peak ordering hour of the day across all orders[cite: 1].
     */
    public Optional<Integer> getPeakOrderingHour() {
        return dataStore.getAllOrders().values().stream()
                .collect(Collectors.groupingBy(
                        order -> order.getPlacedAt().getHour(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /**
     * Report 10: Every customer who has not ordered in the last thirty days[cite: 1].
     */
    public List<Customer> getInactiveCustomersPastThirtyDays(LocalDateTime referenceTime) {
        LocalDateTime cutoff = referenceTime.minusDays(30);

        Map<String, LocalDateTime> latestOrderTimes = dataStore.getAllOrders().values().stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCustomer().getId(),
                        Collectors.mapping(Order::getPlacedAt, Collectors.maxBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));

        return dataStore.getAllCustomers().values().stream()
                .filter(customer -> {
                    LocalDateTime lastOrder = latestOrderTimes.get(customer.getId());
                    return lastOrder == null || lastOrder.isBefore(cutoff);
                })
                .collect(Collectors.toUnmodifiableList());
    }
}