package com.masrdelivery;

import com.masrdelivery.analytics.AnalyticsService;
import com.masrdelivery.console.*;
import com.masrdelivery.data.PlatformDataStore;
import com.masrdelivery.domain.*;
import com.masrdelivery.exception.PlatformException;
import com.masrdelivery.pricing.PercentagePromotion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        PlatformDataStore dataStore = new PlatformDataStore();
        AnalyticsService analyticsService = new AnalyticsService(dataStore);

        seedPlatformData(dataStore);

        CustomerMenuHandler customerHandler = new CustomerMenuHandler(dataStore, analyticsService);
        RestaurantMenuHandler restaurantHandler = new RestaurantMenuHandler(dataStore);
        RiderMenuHandler riderHandler = new RiderMenuHandler(dataStore);
        AdminMenuHandler adminHandler = new AdminMenuHandler(dataStore, analyticsService);

        boolean running = true;
        while (running) {
            System.out.println("===========================================");
            System.out.println("MASR DELIVERY Main Menu");
            System.out.println("===========================================");
            System.out.println("1. Customer Login");
            System.out.println("2. Restaurant Portal");
            System.out.println("3. Rider Portal");
            System.out.println("4. Admin & Reports");
            System.out.println("5. Register New Customer");
            System.out.println("0. Exit");
            System.out.println("===========================================");

            // Increased max option to 5
            int choice = ConsoleUtils.readInt("Choose an option: ", 0, 5);

            switch (choice) {
                case 1: customerHandler.display(); break;
                case 2: restaurantHandler.display(); break;
                case 3: riderHandler.display(); break;
                case 4: adminHandler.display(); break;
                case 5: registerNewCustomer(dataStore); break;
                case 0:
                    System.out.println("Exiting Masr Delivery platform. Goodbye!");
                    running = false;
                    break;
            }
        }
    }

    private static void registerNewCustomer(PlatformDataStore dataStore) {
        ConsoleUtils.printHeader("Register New Customer");

        String name = ConsoleUtils.readString("Enter your full name: ", false);

        Customer newCustomer = null;
        while (newCustomer == null) {
            String mobile = ConsoleUtils.readString("Enter 11-digit mobile (e.g., 010...): ", false);
            // Generate a unique ID using the current time
            String generatedId = "CUST-" + System.currentTimeMillis();

            try {
                // This triggers the regex validation inside the Customer constructor
                newCustomer = new Customer(generatedId, name, mobile);
            } catch (PlatformException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Let's try the phone number again.");
            }
        }

        String district = ConsoleUtils.readString("Enter your District (e.g. Dokki, Maadi, Faisal): ", false);
        String street = ConsoleUtils.readString("Enter your Street Address: ", false);
        newCustomer.addAddresses(new Address(district, street));

        // Give the new user a welcome bonus so they can test ordering immediately
        newCustomer.addFunds(new BigDecimal("1000.00"));

        dataStore.addCustomer(newCustomer);

        System.out.println("\nRegistration Successful!");
        System.out.println("Welcome to Masr Delivery, " + newCustomer.getName() + "!");
        System.out.println("IMPORTANT: Your Customer ID is [" + newCustomer.getId() + "]. Use this to log in.");
        System.out.println("We've added a 1000.00 EGP welcome bonus to your wallet.");
    }

    private static void seedPlatformData(PlatformDataStore dataStore) {
        Customer c1 = new Customer("CUST-101", "Ahmed Ali", "01012345678");
        c1.addAddresses(new Address("Faisal", "15 Faisal Street, Giza"));
        c1.addFunds(new BigDecimal("1000.00"));
        dataStore.addCustomer(c1);

        Restaurant r1 = new Restaurant("REST-01", "Kababgy Maadi", "Maadi", Set.of("Egyptian", "Grill"), 4.8, true);
        MenuItem item1 = MenuItemFactory.createStandard("M1", "Grilled Kofta Plate", new BigDecimal("120.00"), "Grill", 25, true);
        MenuItem item2 = MenuItemFactory.createWeighted("M2", "Mixed Kebab per Kg", new BigDecimal("400.00"), "Grill", 40, true);
        r1.addMenuItem(item1);
        r1.addMenuItem(item2);
        dataStore.addRestaurant(r1);

        Rider rider = new Rider("RIDER-01", "Mahmoud Hassan", new MotorcycleDispatch(), "Maadi");
        dataStore.addRider(rider);

        PercentagePromotion promo = new PercentagePromotion(
                "NILE20", new BigDecimal("0.20"), new BigDecimal("50.00"), BigDecimal.ZERO, LocalDate.now().plusMonths(3), null, false
        );
        dataStore.addPromotion(promo);
    }
}