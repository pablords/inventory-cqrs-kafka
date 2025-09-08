package com.pablords.shared.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StockReservationCancelledEvent {
    private final String orderId;
    private final String productId;
    private final int quantity;

    public StockReservationCancelledEvent(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("productId") String productId,
            @JsonProperty("quantity") int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
