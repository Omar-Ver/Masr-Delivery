package com.masrdelivery.data;

import com.masrdelivery.domain.LoyaltyTier;
import com.masrdelivery.domain.Order;

import java.util.Comparator;

public class ReadyOrderComparator implements Comparator<Order> {
    @Override
    public int compare(Order o1, Order o2) {
        boolean isGold1 = o1.getCustomer().getLoyaltyTier() == LoyaltyTier.GOLD; //[cite: 1]
        boolean isGold2 = o2.getCustomer().getLoyaltyTier() == LoyaltyTier.GOLD; //[cite: 1]

        // Gold tier customers jump ahead of all Bronze and Silver orders[cite: 1]
        if (isGold1 && !isGold2) {
            return -1;
        }
        if (!isGold1 && isGold2) {
            return 1;
        }

        // Within the same tier classification, longest-waiting order is dispatched first[cite: 1]
        return o1.getPlacedAt().compareTo(o2.getPlacedAt());
    }
}