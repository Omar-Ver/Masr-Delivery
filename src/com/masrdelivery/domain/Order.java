package com.masrdelivery.domain;

import com.masrdelivery.exception.IllegalTransitionException;
import com.masrdelivery.exception.PlatformException;
import com.masrdelivery.observer.OrderObserver;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {
    private final String id;
    private final Customer customer;
    private final Restaurant restaurant;
    private final Address deliveryAddress;
    private final List<OrderLineItem> lineItems;
    private final LocalDateTime placedAt;
    private final String promoCode;
    private final String deliveryNotes;
    private OrderStatus status;

    private Rider rider;
    private LocalDateTime deliveredAt;
    private BigDecimal finalTotal = BigDecimal.ZERO;

    // Observer Pattern: Subject state
    private final List<OrderObserver> observers = new ArrayList<>();

    private Order(Builder builder) {
        this.id = builder.id;
        this.customer = builder.customer;
        this.restaurant = builder.restaurant;
        this.deliveryAddress = builder.deliveryAddress;
        this.lineItems = new ArrayList<>(builder.lineItems);
        this.placedAt = builder.placedAt != null ? builder.placedAt : LocalDateTime.now();
        this.promoCode = builder.promoCode;
        this.deliveryNotes = builder.deliveryNotes;
        this.status = OrderStatus.PLACED;

        // Attach observers registered during construction
        if (builder.observers != null) {
            this.observers.addAll(builder.observers);
        }
    }

    public void addObserver(OrderObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(OrderStatus oldStatus, OrderStatus newStatus) {
        // Defensive copy to prevent concurrent modification exceptions if observers are added/removed during broadcast
        for (OrderObserver observer : new ArrayList<>(observers)) {
            observer.update(this, oldStatus, newStatus);
        }
    }

    public synchronized void transitionTo(OrderStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new IllegalTransitionException(
                    "Illegal transition from " + this.status + " to " + nextStatus + " for order " + id
            );
        }

        OrderStatus oldStatus = this.status;
        this.status = nextStatus;

        if (nextStatus == OrderStatus.DELIVERED) {
            this.deliveredAt = LocalDateTime.now();
        }

        // Trigger the Observer Pattern push mechanism
        notifyObservers(oldStatus, nextStatus);
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Restaurant getRestaurant() { return restaurant; }
    public Address getDeliveryAddress() { return deliveryAddress; }
    public List<OrderLineItem> getLineItems() { return Collections.unmodifiableList(lineItems); }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public LocalDateTime placedAt() { return placedAt; }
    public String getPromoCode() { return promoCode; }
    public String getDeliveryNotes() { return deliveryNotes; }
    public OrderStatus getStatus() { return status; }

    public Rider getRider() { return rider; }
    public void setRider(Rider rider) { this.rider = rider; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public BigDecimal getFinalTotal() { return finalTotal; }
    public void setFinalTotal(BigDecimal finalTotal) { this.finalTotal = finalTotal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class Builder {
        private String id;
        private Customer customer;
        private Restaurant restaurant;
        private Address deliveryAddress;
        private List<OrderLineItem> lineItems = new ArrayList<>();
        private LocalDateTime placedAt = LocalDateTime.now();
        private String promoCode;
        private String deliveryNotes;
        private List<OrderObserver> observers = new ArrayList<>();

        public Builder id(String id) { this.id = id; return this; }
        public Builder customer(Customer customer) { this.customer = customer; return this; }
        public Builder restaurant(Restaurant restaurant) { this.restaurant = restaurant; return this; }
        public Builder deliveryAddress(Address deliveryAddress) { this.deliveryAddress = deliveryAddress; return this; }
        public Builder addLineItem(OrderLineItem item) { this.lineItems.add(item); return this; }
        public Builder placedAt(LocalDateTime placedAt) { this.placedAt = placedAt; return this; }
        public Builder promoCode(String promoCode) { this.promoCode = promoCode; return this; }
        public Builder deliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; return this; }

        public Builder addObserver(OrderObserver observer) {
            this.observers.add(observer);
            return this;
        }

        public Order build() {
            if (id == null || id.trim().isEmpty()) { throw new PlatformException("Order ID cannot be null or empty."); }
            if (customer == null) { throw new PlatformException("Order must have a customer."); }
            if (restaurant == null) { throw new PlatformException("Order must have a restaurant."); }
            if (lineItems == null || lineItems.isEmpty()) { throw new PlatformException("An order must contain at least one line item."); }
            if (deliveryAddress == null) { throw new PlatformException("Order must specify a delivery address."); }
            if (!customer.getAddresses().contains(deliveryAddress)) { throw new PlatformException("Delivery address must belong to the customer placing the order."); }
            if (this.placedAt == null) { this.placedAt = LocalDateTime.now(); }

            return new Order(this);
        }
    }
}