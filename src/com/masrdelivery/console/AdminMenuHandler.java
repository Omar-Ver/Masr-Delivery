package com.masrdelivery.console;

import com.masrdelivery.analytics.AnalyticsDTOs.MenuItemFrequency;
import com.masrdelivery.analytics.AnalyticsDTOs.RiderPerformanceReport;
import com.masrdelivery.analytics.AnalyticsService;
import com.masrdelivery.data.PlatformDataStore;
import com.masrdelivery.domain.Customer;
import com.masrdelivery.domain.OrderStatus;
import com.masrdelivery.domain.Restaurant;
import com.masrdelivery.exception.PlatformException;
import com.masrdelivery.pricing.PercentagePromotion;
import com.masrdelivery.pricing.Promotion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

public class AdminMenuHandler {
    private final PlatformDataStore dataStore;
    private final AnalyticsService analyticsService;

    public AdminMenuHandler(PlatformDataStore dataStore, AnalyticsService analyticsService) {
        this.dataStore = dataStore;
        this.analyticsService = analyticsService;
    }

    public void display() {
        boolean running = true;
        while (running) {
            ConsoleUtils.printHeader("Admin & Reports Dashboard"); //[cite: 1]
            System.out.println("1. Add new restaurant"); //[cite: 1]
            System.out.println("2. Create percentage promotion"); //[cite: 1]
            System.out.println("3. Run Total Revenue Report (Date Range)"); //[cite: 1]
            System.out.println("4. Run Top 5 Restaurants by Revenue"); //[cite: 1]
            System.out.println("5. Run Average Order Value per District"); //[cite: 1]
            System.out.println("6. Run High-Rated Popular Restaurants (>4.5, 20+ orders)"); //[cite: 1]
            System.out.println("7. Run Orders Grouped by Status"); //[cite: 1]
            System.out.println("8. Run Rider Performance & Average Durations"); //[cite: 1]
            System.out.println("9. Run Most Frequently Ordered Item"); //[cite: 1]
            System.out.println("10. Run Peak Ordering Hour"); //[cite: 1]
            System.out.println("11. Run Inactive Customers (Past 30 Days)"); //[cite: 1]
            System.out.println("0. Back to Main Menu");
            int choice = ConsoleUtils.readInt("Choose an option: ", 0, 11); //[cite: 1]

            try {
                switch (choice) {
                    case 1: addRestaurant(); break;
                    case 2: createPromotion(); break;
                    case 3: runRevenueReport(); break;
                    case 4: runTopRestaurantsReport(); break;
                    case 5: runAvgDistrictValueReport(); break;
                    case 6: runHighRatedReport(); break;
                    case 7: runOrderStatusCountReport(); break;
                    case 8: runRiderPerformanceReport(); break;
                    case 9: runMostFrequentItemReport(); break;
                    case 10: runPeakHourReport(); break;
                    case 11: runInactiveCustomersReport(); break;
                    case 0: running = false; break;
                }
            } catch (PlatformException e) {
                System.out.println("Admin Error: " + e.getMessage()); //[cite: 1]
            }
        }
    }

    private void addRestaurant() {
        String id = ConsoleUtils.readString("Restaurant ID: ", false); //[cite: 1]
        String name = ConsoleUtils.readString("Name: ", false);
        String district = ConsoleUtils.readString("District (e.g. Maadi, Dokki, Faisal, Nasr City): ", false); //[cite: 1]
        String cuisine = ConsoleUtils.readString("Primary Cuisine Category: ", false); //[cite: 1]
        Restaurant restaurant = new Restaurant(id, name, district, Set.of(cuisine), 5.0, true); //[cite: 1]
        dataStore.addRestaurant(restaurant); //[cite: 1]
        System.out.println("Restaurant " + name + " onboarded successfully.");
    }

    private void createPromotion() {
        String code = ConsoleUtils.readString("Promotion Code: ", false); //[cite: 1]
        BigDecimal rate = ConsoleUtils.readBigDecimal("Discount percentage (e.g., 0.20 for 20%): "); //[cite: 1]
        BigDecimal cap = ConsoleUtils.readBigDecimal("Max discount cap in EGP: "); //[cite: 1]
        BigDecimal minSubtotal = ConsoleUtils.readBigDecimal("Minimum order subtotal: "); //[cite: 1]

        Promotion promo = new PercentagePromotion(code, rate, cap, minSubtotal, LocalDate.now().plusMonths(1), null, false); //[cite: 1]
        dataStore.addPromotion(promo); //[cite: 1]
        System.out.println("Promotion " + code.toUpperCase() + " created.");
    }

    private void runRevenueReport() {
        BigDecimal rev = analyticsService.getTotalRevenue(LocalDate.now().minusDays(30), LocalDate.now()); //[cite: 1]
        System.out.printf("Total Delivered Revenue (Past 30 Days): %.2f EGP%n", rev); //[cite: 1]
    }

    private void runTopRestaurantsReport() {
        var top = analyticsService.getTopFiveRestaurantsByRevenue(LocalDate.now().getYear(), LocalDate.now().getMonth()); //[cite: 1]
        System.out.println("\nTop 5 Restaurants This Month:");
        top.forEach(e -> System.out.printf("- %s: %.2f EGP%n", e.getKey().getName(), e.getValue())); //[cite: 1]
    }

    private void runAvgDistrictValueReport() {
        var map = analyticsService.getAverageOrderValuePerDistrict(); //[cite: 1]
        System.out.println("\nAverage Order Value per District:");
        map.forEach((district, avg) -> System.out.printf("- %s: %.2f EGP%n", district, avg)); //[cite: 1]
    }

    private void runHighRatedReport() {
        var list = analyticsService.getHighRatedPopularRestaurants(); //[cite: 1]
        System.out.println("\nRestaurants with Rating > 4.5 and 20+ Orders:");
        list.forEach(r -> System.out.printf("- %s | Rating: %.1f | Orders: %d%n", r.getName(), r.getRating(), r.getCompletedOrdersCount())); //[cite: 1]
    }

    private void runOrderStatusCountReport() {
        var counts = analyticsService.getOrderCountByStatus(); //[cite: 1]
        System.out.println("\nOrder Counts Grouped by Status:");
        counts.forEach((status, count) -> System.out.printf("- %s: %d%n", status, count)); //[cite: 1]
    }

    private void runRiderPerformanceReport() {
        var riders = analyticsService.getRiderPerformanceReport(); //[cite: 1]
        System.out.println("\nRider Delivery Performance:");
        riders.forEach(r -> System.out.printf("- Rider %s | Deliveries: %d | Avg Duration: %.1f mins%n",
                r.getRider().getName(), r.getCompletedDeliveries(), r.getAverageDurationMinutes())); //[cite: 1]
    }

    private void runMostFrequentItemReport() {
        Optional<MenuItemFrequency> itemOpt = analyticsService.getMostFrequentlyOrderedMenuItem(); //[cite: 1]
        if (itemOpt.isEmpty()) {
            System.out.println("No orders have been placed yet across the platform."); //[cite: 1]
        } else {
            MenuItemFrequency freq = itemOpt.get();
            System.out.printf("Most Frequently Ordered Item: %s (Quantity: %s)%n",
                    freq.getMenuItem().getName(), freq.getTotalQuantityOrdered()); //[cite: 1]
        }
    }

    private void runPeakHourReport() {
        Optional<Integer> peak = analyticsService.getPeakOrderingHour(); //[cite: 1]
        if (peak.isEmpty()) {
            System.out.println("No order data available to determine peak hour.");
        } else {
            System.out.printf("Peak Ordering Hour: %02d:00 - %02d:59%n", peak.get(), peak.get()); //[cite: 1]
        }
    }

    private void runInactiveCustomersReport() {
        var inactives = analyticsService.getInactiveCustomersPastThirtyDays(LocalDateTime.now()); //[cite: 1]
        System.out.println("\nCustomers with No Orders in the Last 30 Days:");
        inactives.forEach(c -> System.out.printf("- Customer ID: %s | Name: %s%n", c.getId(), c.getName())); //[cite: 1]
    }
}