package com.masrdelivery.domain;

import java.math.BigDecimal;

public class MotorcycleDispatch implements DispatchStrategy {
    @Override
    public String getVehicleType() { return "MOTORCYCLE"; } //[cite: 1]

    @Override
    public double getMaxRangeKm() { return 25.0; } //[cite: 1]

    @Override
    public double getAverageSpeedKmh() { return 35.0; } //[cite: 1]

    @Override
    public boolean canHandle(BigDecimal totalWeightKg, int itemCount) {
        return totalWeightKg.compareTo(new BigDecimal("15.0")) <= 0 && itemCount <= 8; //[cite: 1]
    }
}