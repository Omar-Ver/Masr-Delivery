package com.masrdelivery.console;
import com.masrdelivery.data.PlatformDataStore;
import com.masrdelivery.domain.Order;
import com.masrdelivery.domain.OrderStatus;
import com.masrdelivery.domain.Rider;
import com.masrdelivery.exception.PlatformException;

import java.util.Optional;

public class RiderMenuHandler {
    private final PlatformDataStore dataStore;

    public RiderMenuHandler(PlatformDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void display() {
        String riderId = ConsoleUtils.readString("Enter Rider ID: ", false);
        Optional<Rider> optRider = dataStore.findRiderById(riderId);
        if (optRider.isEmpty()) {
            System.out.println("Rider not found. Returning to main menu.");
            return;
        }
        Rider rider = optRider.get();

        boolean running = true;
        while (running) {
            ConsoleUtils.printHeader("Rider Portal - " + rider.getName() + " [Status: " + (rider.isOnDuty() ? "ON DUTY" : "OFF DUTY") + "]");
            // ... rest of the method remains exactly the same
            System.out.println("1. Toggle On/Off Duty"); //[cite: 1]
            System.out.println("2. Poll & assign next available order from queue"); //[cite: 1]
            System.out.println("3. View currently assigned order"); //[cite: 1]
            System.out.println("4. Mark assigned order as OUT FOR DELIVERY"); //[cite: 1]
            System.out.println("5. Mark assigned order as DELIVERED"); //[cite: 1]
            System.out.println("6. View personal delivery statistics"); //[cite: 1]
            System.out.println("0. Back to Main Menu");
            int choice = ConsoleUtils.readInt("Choose an option: ", 0, 6); //[cite: 1]

            try {
                switch (choice) {
                    case 1: toggleDuty(rider); break;
                    case 2: pollAndAssignOrder(rider); break;
                    case 3: viewCurrentOrder(rider); break;
                    case 4: markOutForDelivery(rider); break;
                    case 5: markDelivered(rider); break;
                    case 6: viewStats(rider); break;
                    case 0: running = false; break;
                }
            } catch (PlatformException e) {
                System.out.println("Error: " + e.getMessage()); //[cite: 1]
            }
        }
    }

    private void toggleDuty(Rider rider) {
        rider.setOnDuty(!rider.isOnDuty()); //[cite: 1]
        System.out.printf("Rider duty status is now: %s%n", rider.isOnDuty() ? "ON DUTY" : "OFF DUTY");
    }

    private void pollAndAssignOrder(Rider rider) {
        if (!rider.isAvailable()) {
            throw new PlatformException("Rider is not available to take orders."); //[cite: 1]
        }
        Optional<Order> nextOrder = dataStore.pollNextReadyOrder(); //[cite: 1]
        if (nextOrder.isEmpty()) {
            System.out.println("No READY orders waiting in queue.");
            return;
        }
        Order order = nextOrder.get();
        order.transitionTo(OrderStatus.ASSIGNED); //[cite: 1]
        rider.assignOrder(order); // Enforces single active order constraint[cite: 1]
        System.out.println("Assigned Order ID: " + order.getId() + " from " + order.getRestaurant().getName());
    }

    private void viewCurrentOrder(Rider rider) {
        Optional<Order> current = rider.getCurrentOrder(); //[cite: 1]
        if (current.isEmpty()) {
            System.out.println("No active order currently assigned.");
            return;
        }
        Order o = current.get();
        System.out.printf("Active Order: %s | Status: %s | Restaurant: %s | Delivery District: %s%n",
                o.getId(), o.getStatus(), o.getRestaurant().getName(), o.getDeliveryAddress().getDistrict());
    }

    private void markOutForDelivery(Rider rider) {
        Order current = rider.getCurrentOrder()
                .orElseThrow(() -> new PlatformException("Rider has no active order."));
        current.transitionTo(OrderStatus.OUT_FOR_DELIVERY); //[cite: 1]
        System.out.println("Order status updated to OUT_FOR_DELIVERY.");
    }

    private void markDelivered(Rider rider) {
        Order current = rider.getCurrentOrder()
                .orElseThrow(() -> new PlatformException("Rider has no active order."));
        current.transitionTo(OrderStatus.DELIVERED); //[cite: 1]
        current.getCustomer().incrementCompleteOrders(); // Increment customer's tier count[cite: 1]
        current.getRestaurant().incrementCompletedOrders(); // Increment restaurant's analytics count[cite: 1]
        rider.completeOrder(); // Clears active order slot and increments completed deliveries[cite: 1]
        System.out.println("Order marked DELIVERED and closed successfully.");
    }

    private void viewStats(Rider rider) {
        System.out.printf("Rider %s (%s)%n", rider.getName(), rider.getDispatchStrategy().getVehicleType()); //[cite: 1]
        System.out.printf("Completed deliveries: %d%n", rider.getCompletedDeliveriesCount()); //[cite: 1]
        System.out.printf("Current status: %s%n", rider.isAvailable() ? "AVAILABLE" : "BUSY");
    }
}