package com.masrdelivery.config;

import java.math.BigDecimal;

public class PlatformConfiguration {

    // The volatile keyword guarantees that multiple threads handle the instance correctly
    // by reading from main memory rather than a thread's local cache.
    private static volatile PlatformConfiguration instance;

    // --- Global Platform Settings ---
    private String platformName;
    private BigDecimal defaultServiceFeeRate;
    private double baseDeliveryDistanceKm;
    private BigDecimal baseDeliveryFee;
    private BigDecimal perKmFee;

    // Private constructor prevents instantiation from outside the class
    private PlatformConfiguration() {
        // Defend against reflection API instantiation attacks
        if (instance != null) {
            throw new IllegalStateException("Singleton instance already created.");
        }

        // Initialize default settings
        this.platformName = "Masr Delivery";
        this.defaultServiceFeeRate = new BigDecimal("0.10"); // 10%
        this.baseDeliveryDistanceKm = 3.0;
        this.baseDeliveryFee = new BigDecimal("15.00");
        this.perKmFee = new BigDecimal("3.00");
    }

    // Thread-safe global access point
    public static PlatformConfiguration getInstance() {
        if (instance == null) { // First check (no locking, fast)
            synchronized (PlatformConfiguration.class) {
                if (instance == null) { // Second check (locked, thread-safe)
                    instance = new PlatformConfiguration();
                }
            }
        }
        return instance;
    }

    // --- Getters & Setters ---

    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }

    public BigDecimal getDefaultServiceFeeRate() { return defaultServiceFeeRate; }
    public void setDefaultServiceFeeRate(BigDecimal defaultServiceFeeRate) { this.defaultServiceFeeRate = defaultServiceFeeRate; }

    public double getBaseDeliveryDistanceKm() { return baseDeliveryDistanceKm; }
    public void setBaseDeliveryDistanceKm(double baseDeliveryDistanceKm) { this.baseDeliveryDistanceKm = baseDeliveryDistanceKm; }

    public BigDecimal getBaseDeliveryFee() { return baseDeliveryFee; }
    public void setBaseDeliveryFee(BigDecimal baseDeliveryFee) { this.baseDeliveryFee = baseDeliveryFee; }

    public BigDecimal getPerKmFee() { return perKmFee; }
    public void setPerKmFee(BigDecimal perKmFee) { this.perKmFee = perKmFee; }
}