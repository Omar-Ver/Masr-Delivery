package com.masrdelivery.pricing;

import java.math.BigDecimal;

public class OrderPricingResult {
    private final BigDecimal subtotal; //[cite: 1]
    private final BigDecimal deliveryFee; //[cite: 1]
    private final BigDecimal serviceFee; //[cite: 1]
    private final BigDecimal promotionDiscount; //[cite: 1]
    private final BigDecimal total; //[cite: 1]

    public OrderPricingResult(BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal serviceFee, BigDecimal promotionDiscount, BigDecimal total) {
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.serviceFee = serviceFee;
        this.promotionDiscount = promotionDiscount;
        this.total = total;
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public BigDecimal getPromotionDiscount() { return promotionDiscount; }
    public BigDecimal getTotal() { return total; }

    @Override
    public String toString() {
        return String.format(
                "Subtotal: %.2f EGP | Delivery Fee: %.2f EGP | Service Fee: %.2f EGP | Discount: -%.2f EGP | Total: %.2f EGP",
                subtotal, deliveryFee, serviceFee, promotionDiscount, total
        ); //[cite: 1]
    }
}