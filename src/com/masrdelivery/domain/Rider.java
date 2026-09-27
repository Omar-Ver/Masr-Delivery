package com.masrdelivery.domain;

import com.masrdelivery.exception.PlatformException;
import com.masrdelivery.exception.RiderBusyException;

import java.util.Objects;
import java.util.Optional;

public class Rider {
    private final String id; //[cite: 1]
    private final String name; //[cite: 1]
    private DispatchStrategy dispatchStrategy; //[cite: 1]
    private String currentDistrict; //[cite: 1]
    private boolean onDuty; //[cite: 1]
    private Order currentOrder; //[cite: 1]
    private int completedDeliveriesCount; //[cite: 1]

    public Rider(String id, String name, DispatchStrategy dispatchStrategy, String currentDistrict) {
        if (id == null || id.trim().isEmpty()) {
            throw new PlatformException("Rider ID cannot be null or empty."); //[cite: 1]
        }
        if (name == null || name.trim().isEmpty()) {
            throw new PlatformException("Rider name cannot be null or empty.");
        }
        if (dispatchStrategy == null) {
            throw new PlatformException("Dispatch strategy cannot be null.");
        }
        if (currentDistrict == null || currentDistrict.trim().isEmpty()) {
            throw new PlatformException("Current district cannot be null or empty.");
        }

        this.id = id;
        this.name = name;
        this.dispatchStrategy = dispatchStrategy;
        this.currentDistrict = currentDistrict;
        this.onDuty = true;
        this.currentOrder = null;
        this.completedDeliveriesCount = 0; //[cite: 1]
    }

    /**
     * Enforces the single active order invariant.
     * Only an available rider on duty may be assigned an order.
     */
    public synchronized void assignOrder(Order order) {
        if (!onDuty) {
            throw new PlatformException("Cannot assign order: Rider " + id + " is off duty."); //[cite: 1]
        }
        if (this.currentOrder != null) {
            throw new RiderBusyException(this.id); //[cite: 1]
        }
        if (order == null) {
            throw new PlatformException("Assigned order cannot be null.");
        }
        this.currentOrder = order;
    }

    /**
     * Marks the active order as delivered, clears the slot, and increments deliveries.
     */
    public synchronized void completeOrder() {
        if (this.currentOrder == null) {
            throw new PlatformException("Rider " + id + " has no active order to complete.");
        }
        this.currentOrder = null;
        this.completedDeliveriesCount++; //[cite: 1]
    }

    public void setOnDuty(boolean onDuty) {
        if (!onDuty && this.currentOrder != null) {
            throw new PlatformException("Rider cannot go off duty while carrying an active order.");
        }
        this.onDuty = onDuty;
    }

    public boolean isAvailable() {
        return onDuty && currentOrder == null; //[cite: 1]
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public DispatchStrategy getDispatchStrategy() { return dispatchStrategy; }
    public void setDispatchStrategy(DispatchStrategy dispatchStrategy) { this.dispatchStrategy = dispatchStrategy; }
    public String getCurrentDistrict() { return currentDistrict; }
    public void setCurrentDistrict(String currentDistrict) { this.currentDistrict = currentDistrict; }
    public boolean isOnDuty() { return onDuty; }
    public Optional<Order> getCurrentOrder() { return Optional.ofNullable(currentOrder); } //[cite: 1]
    public int getCompletedDeliveriesCount() { return completedDeliveriesCount; } //[cite: 1]

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rider rider = (Rider) o;
        return Objects.equals(id, rider.id); //[cite: 1]
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}