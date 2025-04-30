package com.pablords.query.domain;

public class StockUpdatedEvent {
    private String productId;
    private int newQuantity;
    private String name;

    public StockUpdatedEvent() {}

    public StockUpdatedEvent(String productId, int newQuantity, String name) {
        this.productId = productId;
        this.newQuantity = newQuantity;
        this.name = name;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(int newQuantity) {
        this.newQuantity = newQuantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}