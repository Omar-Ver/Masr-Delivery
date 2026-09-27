package com.masrdelivery.console;

import com.masrdelivery.analytics.AnalyticsDTOs.CustomerOrderHistoryReport;
import com.masrdelivery.analytics.AnalyticsService;
import com.masrdelivery.data.PlatformDataStore;
import com.masrdelivery.domain.*;
import com.masrdelivery.exception.PlatformException;
import com.masrdelivery.pricing.OrderPricingResult;
import com.masrdelivery.pricing.PricingEngine;
import com.masrdelivery.pricing.Promotion;
import com.masrdelivery.search.RestaurantPredicates;
import com.masrdelivery.search.RestaurantSearchService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerMenuHandler {
    private final PlatformDataStore dataStore;
    private final AnalyticsService analyticsService;

    public CustomerMenuHandler(PlatformDataStore dataStore, AnalyticsService analyticsService) {
        this.dataStore = dataStore;
        this.analyticsService = analyticsService;
    }

    public void display() {
        String customerId = ConsoleUtils.readString("Enter your Customer ID: ", false);
        Optional<Customer> optCustomer = dataStore.findCustomerById(customerId);
        if (optCustomer.isEmpty()) {
            System.out.println("Customer not found. Returning to main menu.");
            return;
        }
        Customer customer = optCustomer.get();

        boolean running = true;
        while (running) {
            ConsoleUtils.printHeader("Customer Menu - Logged in as: " + customer.getId() + " (" + customer.getLoyaltyTier() + ")"); //[cite: 1]
            System.out.println("1. Browse open restaurants"); //[cite: 1]
            System.out.println("2. Search restaurants by text"); //[cite: 1]
            System.out.println("3. View restaurant menu"); //[cite: 1]
            System.out.println("4. Place new order"); //[cite: 1]
            System.out.println("5. Pay order from wallet"); //[cite: 1]
            System.out.println("6. Track order"); //[cite: 1]
            System.out.println("7. Cancel order"); //[cite: 1]
            System.out.println("8. View order history"); //[cite: 1]
            System.out.println("0. Back to Main Menu");
            int choice = ConsoleUtils.readInt("Choose an option: ", 0, 8); //[cite: 1]

            try {
                switch (choice) {
                    case 1: browseRestaurants(); break;
                    case 2: searchRestaurants(customer); break;
                    case 3: viewMenu(); break;
                    case 4: placeOrder(customer); break;
                    case 5: payFromWallet(customer); break;
                    case 6: trackOrder(); break;
                    case 7: cancelOrder(customer); break;
                    case 8: viewHistory(customer); break;
                    case 0: running = false; break;
                }
            } catch (PlatformException e) {
                System.out.println("Operation Failed: " + e.getMessage()); //[cite: 1]
            }
        }
    }

    private void browseRestaurants() {
        List<Restaurant> openRestaurants = RestaurantSearchService.search(
                dataStore.getAllRestaurants().values(),
                RestaurantPredicates.isOpen() //[cite: 1]
        );
        if (openRestaurants.isEmpty()) {
            System.out.println("No open restaurants available.");
            return;
        }
        int index = 1;
        for (Restaurant r : openRestaurants) {
            System.out.printf("%d. [%s] %s | District: %s | Cuisines: %s | Rating: %.1f%n",
                    index++, r.getId(), r.getName(), r.getDistrict(), r.getCuisines(), r.getRating()); //[cite: 1]
        }
    }

    private void searchRestaurants(Customer customer) {
        String keyword = ConsoleUtils.readString("Enter search keyword (name or cuisine): ", false); //[cite: 1]
        dataStore.logCustomerSearch(customer.getId(), keyword); //[cite: 1]
        List<Restaurant> results = RestaurantSearchService.search(
                dataStore.getAllRestaurants().values(),
                RestaurantPredicates.byFreeText(keyword) //[cite: 1]
        );
        if (results.isEmpty()) {
            System.out.println("Nothing found matching your query."); //[cite: 1]
            return;
        }
        results.forEach(r -> System.out.printf("- %s (%s) | Cuisines: %s | Rating: %.1f%n",
                r.getName(), r.getDistrict(), r.getCuisines(), r.getRating()));
    }

    private void viewMenu() {
        String restaurantId = ConsoleUtils.readString("Enter Restaurant ID: ", false); //[cite: 1]
        Restaurant restaurant = dataStore.findRestaurantById(restaurantId)
                .orElseThrow(() -> new PlatformException("Restaurant not found."));
        System.out.println("\n--- Menu for " + restaurant.getName() + " ---");
        restaurant.getMenu().forEach(item -> { // Displays in insertion order[cite: 1]
            String status = item.isAvailable() ? "AVAILABLE" : "UNAVAILABLE"; //[cite: 1]
            System.out.printf("[%s] %s (%s) - Prep: %d mins [%s]%n",
                    item.getId(), item.getName(), item.getCategory(), item.getPrepTimeMinutes(), status);
        });
    }

    private void placeOrder(Customer customer) {
        String restaurantId = ConsoleUtils.readString("Enter Restaurant ID: ", false);
        Restaurant restaurant = dataStore.findRestaurantById(restaurantId)
                .orElseThrow(() -> new PlatformException("Restaurant not found."));

        if (!restaurant.isOpen()) {
            throw new PlatformException("Cannot place order: Restaurant is currently closed."); //[cite: 1]
        }
        if (customer.getAddresses().isEmpty()) {
            throw new PlatformException("Customer has no saved addresses.");
        }

        Address address = customer.getAddresses().get(0); // Uses first saved address[cite: 1]
        String orderId = "ORD-" + System.currentTimeMillis();
        Order.Builder builder = new Order.Builder()
                .id(orderId) //[cite: 1]
                .customer(customer) //[cite: 1]
                .restaurant(restaurant) //[cite: 1]
                .deliveryAddress(address) //[cite: 1]
                .placedAt(LocalDateTime.now()); //[cite: 1]

        boolean addingItems = true;
        while (addingItems) {
            String itemId = ConsoleUtils.readString("Enter Menu Item ID to add: ", false);
            MenuItem item = restaurant.getMenu().stream()
                    .filter(m -> m.getId().equalsIgnoreCase(itemId))
                    .findFirst()
                    .orElseThrow(() -> new PlatformException("Item not found on this menu."));

            if (!item.isAvailable()) {
                throw new PlatformException("Cannot order unavailable item: " + item.getName()); //[cite: 1]
            }

            BigDecimal qty = ConsoleUtils.readBigDecimal("Enter quantity / kg: "); //[cite: 1]
            builder.addLineItem(new OrderLineItem(item, qty)); //[cite: 1]

            String more = ConsoleUtils.readString("Add another item? (y/n): ", false);
            if (!more.equalsIgnoreCase("y")) {
                addingItems = false;
            }
        }

        String promoInput = ConsoleUtils.readString("Enter promotion code (optional, press Enter to skip): ", true); //[cite: 1]
        Promotion promo = promoInput.isEmpty() ? null : dataStore.findPromotionByCode(promoInput).orElse(null); //[cite: 1]

        Order order = builder.promoCode(promo != null ? promo.getCode() : null).build(); //[cite: 1]
        OrderPricingResult pricing = PricingEngine.calculateOrderTotal(order, promo); //[cite: 1]

        dataStore.addOrder(order); //[cite: 1]
        System.out.println("\nOrder Placed Successfully!");
        System.out.println("Order Reference: " + order.getId()); //[cite: 1]
        System.out.println("Summary: " + pricing); //[cite: 1]
    }

    private void payFromWallet(Customer customer) {
        String orderId = ConsoleUtils.readString("Enter Order Reference: ", false); //[cite: 1]
        Order order = dataStore.findOrderById(orderId)
                .orElseThrow(() -> new PlatformException("Order not found."));

        Promotion promo = order.getPromoCode() != null ? dataStore.findPromotionByCode(order.getPromoCode()).orElse(null) : null;
        OrderPricingResult pricing = PricingEngine.calculateOrderTotal(order, promo); //[cite: 1]

        customer.deductFunds(pricing.getTotal()); // Atomic balance deduction or InsufficientWalletBalanceException[cite: 1]
        System.out.printf("Payment successful. Deducted %.2f EGP. Remaining wallet balance: %.2f EGP%n",
                pricing.getTotal(), customer.getWalletBalance()); //[cite: 1]
    }

    private void trackOrder() {
        String orderId = ConsoleUtils.readString("Enter Order Reference: ", false); //[cite: 1]
        Order order = dataStore.findOrderById(orderId)
                .orElseThrow(() -> new PlatformException("Order not found."));

        long elapsedMinutes = Duration.between(order.getPlacedAt(), LocalDateTime.now()).toMinutes(); //[cite: 1]
        System.out.printf("Order ID: %s | Status: %s | Elapsed Time: %d minutes ago%n",
                order.getId(), order.getStatus(), elapsedMinutes); //[cite: 1]
    }

    private void cancelOrder(Customer customer) {
        String orderId = ConsoleUtils.readString("Enter Order Reference to cancel: ", false); //[cite: 1]
        Order order = dataStore.findOrderById(orderId)
                .orElseThrow(() -> new PlatformException("Order not found."));

        order.transitionTo(OrderStatus.CANCELLED); // Enforces state machine rules[cite: 1]
        Promotion promo = order.getPromoCode() != null ? dataStore.findPromotionByCode(order.getPromoCode()).orElse(null) : null;
        OrderPricingResult pricing = PricingEngine.calculateOrderTotal(order, promo);
        customer.addFunds(pricing.getTotal()); // Refund to wallet[cite: 1]
        System.out.printf("Order %s has been cancelled and %.2f EGP refunded to your wallet.%n", order.getId(), pricing.getTotal()); //[cite: 1]
    }

    private void viewHistory(Customer customer) {
        CustomerOrderHistoryReport report = analyticsService.getCustomerOrderHistory(customer.getId()); //[cite: 1]
        System.out.println("\n--- Order History for Customer " + customer.getId() + " ---");
        report.getOrders().forEach(o -> System.out.printf("[%s] Placed: %s | Status: %s%n",
                o.getId(), o.getPlacedAt(), o.getStatus())); // Newest first[cite: 1]
        System.out.printf("Lifetime Total Spent: %.2f EGP%n", report.getTotalSpent()); //[cite: 1]
    }
}