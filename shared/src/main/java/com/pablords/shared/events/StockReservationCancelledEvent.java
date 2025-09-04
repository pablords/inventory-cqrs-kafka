package com.pablords.shared.events;


public class StockReservationCancelledEvent {
    private final String orderId;
    private final String productId;
    private final int quantity;

    public StockReservationCancelledEvent(String orderId, String productId, int quantity) {
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
