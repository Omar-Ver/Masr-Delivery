package com.masrdelivery.console;

import com.masrdelivery.data.PlatformDataStore;
import com.masrdelivery.domain.*;
import com.masrdelivery.exception.PlatformException;
import com.masrdelivery.pricing.PricingEngine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RestaurantMenuHandler {
    private final PlatformDataStore dataStore;

    public RestaurantMenuHandler(PlatformDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void display() {
        String restId = ConsoleUtils.readString("Enter Restaurant ID: ", false);
        Optional<Restaurant> optRestaurant = dataStore.findRestaurantById(restId);
        if (optRestaurant.isEmpty()) {
            System.out.println("Restaurant not found. Returning to main menu.");
            return;
        }
        Restaurant restaurant = optRestaurant.get();

        boolean running = true;
        while (running) {
            ConsoleUtils.printHeader("Restaurant Portal - " + restaurant.getName());
            // ... rest of the method remains exactly the same
            System.out.println("1. View pending orders & accept/reject");
            System.out.println("2. Update preparing order to READY");
            System.out.println("3. Toggle menu item availability");
            System.out.println("4. Add standard menu item");
            System.out.println("5. Remove menu item");
            System.out.println("6. View today's orders & revenue");
            System.out.println("0. Back to Main Menu");
            int choice = ConsoleUtils.readInt("Choose an option: ", 0, 6);

            try {
                switch (choice) {
                    case 1: managePendingOrders(restaurant); break;
                    case 2: advanceOrderToReady(restaurant); break;
                    case 3: toggleAvailability(restaurant); break;
                    case 4: addStandardMenuItem(restaurant); break;
                    case 5: removeMenuItem(restaurant); break;
                    case 6: viewDailySummary(restaurant); break;
                    case 0: running = false; break;
                }
            } catch (PlatformException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void managePendingOrders(Restaurant restaurant) {
        List<Order> pending = dataStore.getAllOrders().values().stream()
                .filter(o -> o.getRestaurant().equals(restaurant) && o.getStatus() == OrderStatus.PLACED)
                .collect(Collectors.toList());

        if (pending.isEmpty()) {
            System.out.println("No pending orders awaiting review.");
            return;
        }

        pending.forEach(o -> System.out.printf("Order ID: %s | Items count: %d%n", o.getId(), o.getLineItems().size()));
        String orderId = ConsoleUtils.readString("Enter Order ID to process: ", false);
        Order order = pending.stream().filter(o -> o.getId().equalsIgnoreCase(orderId)).findFirst()
                .orElseThrow(() -> new PlatformException("Order not in pending list."));

        int action = ConsoleUtils.readInt("1. Accept | 2. Reject: ", 1, 2);
        if (action == 1) {
            order.transitionTo(OrderStatus.ACCEPTED);
            order.transitionTo(OrderStatus.PREPARING);
            System.out.println("Order accepted and marked PREPARING.");
        } else {
            order.transitionTo(OrderStatus.CANCELLED);
            System.out.println("Order rejected and CANCELLED.");
        }
    }

    private void advanceOrderToReady(Restaurant restaurant) {
        String orderId = ConsoleUtils.readString("Enter Order ID to mark READY: ", false);
        Order order = dataStore.findOrderById(orderId)
                .orElseThrow(() -> new PlatformException("Order not found."));

        if (!order.getRestaurant().equals(restaurant)) {
            throw new PlatformException("Order does not belong to this restaurant.");
        }

        order.transitionTo(OrderStatus.READY);
        dataStore.enqueueReadyOrder(order);
        System.out.println("Order marked READY and queued for dispatch.");
    }

    private void toggleAvailability(Restaurant restaurant) {
        String itemId = ConsoleUtils.readString("Enter Menu Item ID to toggle: ", false);
        MenuItem item = restaurant.getMenu().stream()
                .filter(m -> m.getId().equalsIgnoreCase(itemId))
                .findFirst()
                .orElseThrow(() -> new PlatformException("Menu item not found."));

        item.setAvailable(!item.isAvailable());
        System.out.printf("Item '%s' availability is now: %s%n", item.getName(), item.isAvailable() ? "AVAILABLE" : "UNAVAILABLE");
    }

    private void addStandardMenuItem(Restaurant restaurant) {
        String id = ConsoleUtils.readString("Item ID: ", false);
        String name = ConsoleUtils.readString("Item Name: ", false);
        BigDecimal price = ConsoleUtils.readBigDecimal("Price in EGP: ");
        String category = ConsoleUtils.readString("Category: ", false);
        int prepTime = ConsoleUtils.readInt("Preparation time in minutes: ", 1, 180);

        // Fixed: Use createStandard instead of createFromRecord
        MenuItem item = MenuItemFactory.createStandard(id, name, price, category, prepTime, true);
        restaurant.addMenuItem(item);
        System.out.println("Standard item added to menu.");
    }

    private void removeMenuItem(Restaurant restaurant) {
        String itemId = ConsoleUtils.readString("Enter Menu Item ID to remove: ", false);
        restaurant.removeMenuItem(itemId);
        System.out.println("Item removed from menu.");
    }

    private void viewDailySummary(Restaurant restaurant) {
        LocalDate today = LocalDate.now();
        List<Order> todaysOrders = dataStore.getAllOrders().values().stream()
                .filter(o -> o.getRestaurant().equals(restaurant))
                .filter(o -> o.getPlacedAt().toLocalDate().equals(today))
                .collect(Collectors.toList());

        BigDecimal revenue = todaysOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(o -> PricingEngine.calculateOrderTotal(o, null).getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.printf("Total orders today: %d | Total revenue: %.2f EGP%n", todaysOrders.size(), revenue);
    }
}