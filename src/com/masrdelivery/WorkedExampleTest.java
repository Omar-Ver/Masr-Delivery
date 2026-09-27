package com.masrdelivery;

import com.masrdelivery.domain.*;
import com.masrdelivery.pricing.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class WorkedExampleTest {
    public static void main(String[] args) {
        System.out.println("Running Part B Worked Example Verification...\n");

        // 1. Arrange Customer (Bronze Tier, Address in Faisal)
        Customer customer = new Customer("CUST-101", "Ahmed Ali", "01012345678");
        Address deliveryAddress = new Address("Faisal", "15 Faisal Street, Giza");
        customer.addAddresses(deliveryAddress); // Fixed: singular 'addAddress'

        // 2. Arrange Restaurant in Maadi
        Restaurant restaurant = new Restaurant("REST-01", "Kababgy Maadi", "Maadi", java.util.Set.of("Egyptian", "Grill"), 4.8, true);
        MenuItem item = MenuItemFactory.createStandard("M1", "Grilled Kofta Plate", new BigDecimal("120.00"), "Grill", 25, true);
        restaurant.addMenuItem(item);

        // 3. Build Order (2 x 120.00 EGP = 240.00 EGP subtotal)
        Order order = new Order.Builder()
                .id("ORD-WORKED-EXAMPLE")
                .customer(customer)
                .restaurant(restaurant)
                .deliveryAddress(deliveryAddress)
                .addLineItem(new OrderLineItem(item, new BigDecimal("2")))
                .promoCode("NILE20")
                .build();

        // 4. Promo NILE20: 20% off subtotal, max cap 50.00 EGP
        Promotion promo = new PercentagePromotion(
                "NILE20",
                new BigDecimal("0.20"),
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                LocalDate.now().plusMonths(1),
                null,
                false
        );

        // 5. Calculate
        OrderPricingResult result = PricingEngine.calculateOrderTotal(order, promo);

        // 6. Assertions
        assertPrice("Subtotal", new BigDecimal("240.00"), result.getSubtotal());
        assertPrice("Delivery Fee", new BigDecimal("42.00"), result.getDeliveryFee());
        assertPrice("Service Fee", new BigDecimal("24.00"), result.getServiceFee());
        assertPrice("Promotion Discount", new BigDecimal("48.00"), result.getPromotionDiscount());
        assertPrice("Final Total", new BigDecimal("258.00"), result.getTotal());

        System.out.println("\nAll assertions passed successfully! Total is exactly 258.00 EGP.");
    }

    private static void assertPrice(String label, BigDecimal expected, BigDecimal actual) {
        if (expected.compareTo(actual) != 0) {
            throw new AssertionError(String.format("FAILED %s: Expected %.2f EGP, but got %.2f EGP", label, expected, actual));
        }
        System.out.printf("PASS: %s = %.2f EGP%n", label, actual);
    }
}