package com.masrdelivery.pricing;

import com.masrdelivery.config.PlatformConfiguration;
import com.masrdelivery.domain.LoyaltyTier;
import com.masrdelivery.domain.Order;
import com.masrdelivery.domain.OrderLineItem;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PricingEngine {

    public static OrderPricingResult calculateOrderTotal(Order order, Promotion promotion) {
        // Fetch dynamic settings from the Singleton instance
        PlatformConfiguration config = PlatformConfiguration.getInstance();

        // Step 1: Subtotal
        BigDecimal subtotal = order.getLineItems().stream()
                .map(OrderLineItem::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Step 2: Delivery fee calculation
        double distanceKm = DistrictDistanceService.getDistanceKm(
                order.getRestaurant().getDistrict(),
                order.getDeliveryAddress().getDistrict()
        );

        BigDecimal rawDeliveryFee = config.getBaseDeliveryFee();
        if (distanceKm > config.getBaseDeliveryDistanceKm()) {
            BigDecimal extraKm = BigDecimal.valueOf(distanceKm - config.getBaseDeliveryDistanceKm());
            rawDeliveryFee = rawDeliveryFee.add(extraKm.multiply(config.getPerKmFee()));
        }

        // Apply Loyalty tier benefit
        LoyaltyTier tier = order.getCustomer().getLoyaltyTier();
        BigDecimal deliveryFee = rawDeliveryFee;
        if (tier == LoyaltyTier.GOLD) {
            deliveryFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP); // Fee waived
        } else if (tier == LoyaltyTier.SILVER) {
            deliveryFee = deliveryFee.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP); // 10% off
        } else {
            deliveryFee = deliveryFee.setScale(2, RoundingMode.HALF_UP);
        }

        // Step 3: Service fee (Configurable rate)
        BigDecimal serviceFee = subtotal.multiply(config.getDefaultServiceFeeRate()).setScale(2, RoundingMode.HALF_UP);

        // Step 4: Promotion discount
        BigDecimal promoDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (promotion != null) {
            promotion.validateConditions(
                    order.getCustomer(),
                    subtotal,
                    order.getDeliveryAddress().getDistrict()
            );

            if (promotion.isFreeDelivery()) {
                promoDiscount = deliveryFee;
                deliveryFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else {
                promoDiscount = promotion.calculateDiscount(subtotal, deliveryFee);
            }
        }

        // Step 5: Total
        BigDecimal total = subtotal.add(deliveryFee).add(serviceFee).subtract(promoDiscount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return new OrderPricingResult(subtotal, deliveryFee, serviceFee, promoDiscount, total);
    }
}