package com.pablords.command.domain;

public class StockUpdatedEvent {
  private final String productId;
  private final int newQuantity;
  private final String name;

  public StockUpdatedEvent(String productId, int newQuantity, String name) {
    this.productId = productId;
    this.newQuantity = newQuantity;
    this.name = name;
  }

  public String getProductId() {
    return productId;
  }

  public int getNewQuantity() {
    return newQuantity;
  }

  public String getName() {
    return name;
  }
}
