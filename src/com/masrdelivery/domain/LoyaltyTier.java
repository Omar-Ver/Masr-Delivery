package com.masrdelivery.domain;

public enum LoyaltyTier {
    BRONZE, SILVER, GOLD;

    public static LoyaltyTier fromOrderCount(int completedOrders) {
        if (completedOrders >= 30) return GOLD;
        if (completedOrders >= 10) return SILVER;
        return BRONZE;
    }

}
