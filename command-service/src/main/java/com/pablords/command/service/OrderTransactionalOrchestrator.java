package com.pablords.command.service;

import java.util.UUID;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablords.command.model.Outbox;
import com.pablords.command.repository.OutboxRepository;
import com.pablords.shared.events.StockReservationCancelledEvent;
import java.time.Instant;
import java.util.Map;

import com.pablords.command.model.Order;
import com.pablords.command.model.Product;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderTransactionalOrchestrator {

  private final ProductService productService;
  private final OrderService orderService;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public OrderTransactionalOrchestrator(ProductService productService, OrderService orderService, OutboxRepository outboxRepository) {
    this.productService = productService;
    this.orderService = orderService;
    this.outboxRepository = outboxRepository;
  }

  @Retryable(value = { ObjectOptimisticLockingFailureException.class,
      RuntimeException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
  @Transactional
  public Order processOrder(UUID productId, int quantity) {
    Product product = productService.removeStock(productId, quantity);
    Order order = orderService.createOrder(product.getId(), quantity);
    log.info("Order {} created for product {}", order.getId(), productId);
    return order;
  }

  private void saveCompensatingOutbox(UUID productId, int quantity) {
    Map<String, Object> payload = objectMapper.convertValue(
        new StockReservationCancelledEvent(UUID.randomUUID().toString(), productId.toString(), quantity),
        Map.class);
    Outbox event = new Outbox();
    event.setId(UUID.randomUUID());
    event.setAggregateType("Stock");
    event.setAggregateId(productId.toString());
    event.setType("stock-reservation-cancelled");
    event.setPayload(payload);
    event.setCreatedAt(Instant.now());
    outboxRepository.save(event);
    log.info("Compensating outbox event created: {}", event.getId());
  }

  @Recover
  public void recover(ObjectOptimisticLockingFailureException ex, UUID productId, int quantity) {
    log.error("Failed to process order after retries. Compensating...");
    saveCompensatingOutbox(productId, quantity);
  }

  @Recover
  public Order recover(RuntimeException ex, UUID productId, int quantity) {
    log.error("Failed to process order after retries. Compensating...");
    saveCompensatingOutbox(productId, quantity);
    return null;
  }

}
