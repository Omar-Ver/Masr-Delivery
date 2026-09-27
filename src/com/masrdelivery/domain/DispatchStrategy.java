package com.masrdelivery.domain;

import java.math.BigDecimal;

public interface DispatchStrategy {
    String getVehicleType(); //
    double getMaxRangeKm(); //
    double getAverageSpeedKmh(); //
    boolean canHandle(BigDecimal totalWeightKg, int itemCount); //[cite: 1]
}