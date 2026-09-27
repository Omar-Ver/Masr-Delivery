package com.masrdelivery.data;

import com.masrdelivery.domain.Customer;
import com.masrdelivery.domain.Order;
import com.masrdelivery.domain.Restaurant;
import com.masrdelivery.domain.Rider;
import com.masrdelivery.exception.PlatformException;
import com.masrdelivery.pricing.Promotion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

public class PlatformDataStore {
    // HashMaps ensure lookup by identifier does not scan every record[cite: 1]
    private final Map<String, Restaurant> restaurants = new HashMap<>(); //[cite: 1]
    private final Map<String, Customer> customers = new HashMap<>(); //[cite: 1]
    private final Map<String, Rider> riders = new HashMap<>(); //[cite: 1]
    private final Map<String, Order> orders = new HashMap<>(); //[cite: 1]
    private final Map<String, Promotion> promotions = new HashMap<>(); //[cite: 1]

    // Customer search histories keyed by Customer ID[cite: 1]
    private final Map<String, CustomerSearchHistory> customerSearches = new HashMap<>();

    // Priority queue dispatching ready orders using priority rules[cite: 1]
    private final PriorityQueue<Order> readyOrderQueue = new PriorityQueue<>(new ReadyOrderComparator()); //[cite: 1]

    // --- Entity Registration and Retrieval ---

    public void addRestaurant(Restaurant restaurant) {
        if (restaurants.containsKey(restaurant.getId())) {
            throw new PlatformException("Duplicate restaurant ID: " + restaurant.getId()); //[cite: 1]
        }
        restaurants.put(restaurant.getId(), restaurant);
    }

    public Optional<Restaurant> findRestaurantById(String id) {
        return Optional.ofNullable(restaurants.get(id)); //[cite: 1]
    }

    public void addCustomer(Customer customer) {
        if (customers.containsKey(customer.getId())) {
            throw new PlatformException("Duplicate customer ID: " + customer.getId()); //[cite: 1]
        }
        customers.put(customer.getId(), customer);
        customerSearches.put(customer.getId(), new CustomerSearchHistory());
    }

    public Optional<Customer> findCustomerById(String id) {
        return Optional.ofNullable(customers.get(id)); //[cite: 1]
    }

    public void addRider(Rider rider) {
        if (riders.containsKey(rider.getId())) {
            throw new PlatformException("Duplicate rider ID: " + rider.getId()); //[cite: 1]
        }
        riders.put(rider.getId(), rider);
    }

    public Optional<Rider> findRiderById(String id) {
        return Optional.ofNullable(riders.get(id)); //[cite: 1]
    }

    public void addOrder(Order order) {
        if (orders.containsKey(order.getId())) {
            throw new PlatformException("Duplicate order ID: " + order.getId()); //[cite: 1]
        }
        orders.put(order.getId(), order);
    }

    public Optional<Order> findOrderById(String id) {
        return Optional.ofNullable(orders.get(id)); //[cite: 1]
    }

    public void addPromotion(Promotion promo) {
        promotions.put(promo.getCode().toUpperCase(), promo); //[cite: 1]
    }

    public Optional<Promotion> findPromotionByCode(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(promotions.get(code.toUpperCase())); //[cite: 1]
    }

    // --- Queue Management ---

    public synchronized void enqueueReadyOrder(Order order) {
        readyOrderQueue.offer(order); //[cite: 1]
    }

    public synchronized Optional<Order> pollNextReadyOrder() {
        return Optional.ofNullable(readyOrderQueue.poll()); //[cite: 1]
    }

    // --- Search Tracking ---

    public void logCustomerSearch(String customerId, String query) {
        CustomerSearchHistory history = customerSearches.computeIfAbsent(customerId, k -> new CustomerSearchHistory());
        history.recordSearch(query); //[cite: 1]
    }

    public List<String> getCustomerRecentSearches(String customerId) {
        CustomerSearchHistory history = customerSearches.get(customerId);
        if (history == null) {
            return Collections.emptyList();
        }
        return history.getRecentSearches(); //[cite: 1]
    }

    // --- Platform Queries & Listings ---

    public Set<String> getAllDistinctCuisines() {
        // Collect distinct cuisines across all restaurants with no duplicates[cite: 1]
        return restaurants.values().stream()
                .flatMap(restaurant -> restaurant.getCuisines().stream())
                .collect(Collectors.toUnmodifiableSet()); //[cite: 1]
    }

    public List<Restaurant> getRestaurantsSortedByRating() {
        // Sorted by rating descending, ties broken alphabetically[cite: 1]
        return restaurants.values().stream()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    // --- Unmodifiable Defensive Views ---

    public Map<String, Restaurant> getAllRestaurants() {
        return Collections.unmodifiableMap(restaurants); //[cite: 1]
    }

    public Map<String, Customer> getAllCustomers() {
        return Collections.unmodifiableMap(customers); //[cite: 1]
    }

    public Map<String, Rider> getAllRiders() {
        return Collections.unmodifiableMap(riders); //[cite: 1]
    }

    public Map<String, Order> getAllOrders() {
        return Collections.unmodifiableMap(orders); //[cite: 1]
    }
}