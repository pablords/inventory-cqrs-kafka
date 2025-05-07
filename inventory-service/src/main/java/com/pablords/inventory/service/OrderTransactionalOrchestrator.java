package com.pablords.inventory.service;

import java.util.UUID;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Service;

import com.pablords.inventory.model.Order;
import com.pablords.inventory.model.Product;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderTransactionalOrchestrator {

  private final ProductService productService;
  private final OrderService orderService;

  public OrderTransactionalOrchestrator(ProductService productService, OrderService orderService) {
    this.productService = productService;
    this.orderService = orderService;
  }

  @Retryable(value = { ObjectOptimisticLockingFailureException.class,
      RuntimeException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
  @Transactional
  public Order processOrder(UUID productId, int quantity) {
    if (quantity <= 0)
      throw new IllegalArgumentException("Quantity must be greater than 0");

    Product product = productService.removeStock(productId, quantity);
    Order order = orderService.createOrder(product.getId(), quantity);

    log.info("Order {} created for product {}", order.getId(), productId);
    return order;
  }

  @Recover
  public void recover(ObjectOptimisticLockingFailureException ex, UUID productId, int quantity) {
    log.error("Failed to process order after retries. Compensating...");
  }

  @Recover
  public Order recover(RuntimeException ex, UUID productId, int quantity) {
    log.error("Failed to process order after retries. Compensating...");
    return null;
  }

}
