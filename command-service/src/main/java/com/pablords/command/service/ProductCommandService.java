package com.pablords.command.service;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablords.command.domain.OutboxEvent;
import com.pablords.command.domain.Product;
import com.pablords.shared.events.StockUpdatedEvent;
import com.pablords.command.repository.OutboxEventRepository;
import com.pablords.command.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductCommandService {

  private static final Logger log = LoggerFactory.getLogger(ProductCommandService.class);

  private final ProductRepository productRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final KafkaTemplate<String, StockUpdatedEvent> kafkaTemplate;

  public ProductCommandService(ProductRepository productRepository,
      @Qualifier("stockUpdatedKafkaTemplate") KafkaTemplate<String, StockUpdatedEvent> kafkaTemplate,
      OutboxEventRepository outboxEventRepository) {
    this.productRepository = productRepository;
    this.kafkaTemplate = kafkaTemplate;
    this.outboxEventRepository = outboxEventRepository;
  }

  @Transactional
  public Product createProduct(String name, int initialQty) {
    try {
      Product product = new Product(name, initialQty);
      Product saved = productRepository.save(product);
      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> payload = objectMapper.convertValue(
          new StockUpdatedEvent(saved.getId().toString(), saved.getQuantity(), saved.getName()),
          Map.class);

      OutboxEvent event = new OutboxEvent();
      event.setId(UUID.randomUUID());
      event.setAggregateType("Stock");
      event.setAggregateId(saved.getId().toString());
      event.setType("stock-updated");
      event.setPayload(payload);
      event.setCreatedAt(Instant.now());
      log.info("Outbox event created: {}", event.getId());

      return saved;
    } catch (ObjectOptimisticLockingFailureException ex) {
      throw new RuntimeException("Concurrency conflict detected (optimistic lock)");
    }
  }

  @Transactional
  public Product addStock(UUID productId, int amount) {
    log.info("Adding stock to product: {}", productId);
    try {
      Product product = productRepository.findById(productId)
          .orElseThrow(() -> new RuntimeException("Product not found"));
      product.addStock(amount);

      Product updated = productRepository.save(product);
      log.info("Product updated: {}", updated.getId());

      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> payload = objectMapper.convertValue(
          new StockUpdatedEvent(updated.getId().toString(), updated.getQuantity(), updated.getName()),
          Map.class);

      OutboxEvent event = new OutboxEvent();
      event.setId(UUID.randomUUID());
      event.setAggregateType("Stock");
      event.setAggregateId(updated.getId().toString());
      event.setType("stock-updated");
      event.setPayload(payload);
      event.setCreatedAt(Instant.now());
      log.info("Outbox event created: {}", event.getId());

      outboxEventRepository.save(event);
      return updated;
    } catch (ObjectOptimisticLockingFailureException ex) {
      // Este é o caso do Optimistic Lock falhar
      // Ex.: outro processo atualizou o registro primeiro.
      throw new RuntimeException("Concurrency conflict detected (optimistic lock)");
    }
  }

  @Transactional
  public Product removeStock(UUID productId, int amount) {
    try {
      Product product = productRepository.findById(productId)
          .orElseThrow(() -> new RuntimeException("Product not found"));
      product.removeStock(amount);
      Product updated = productRepository.save(product);

      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> payload = objectMapper.convertValue(
          new StockUpdatedEvent(updated.getId().toString(), updated.getQuantity(), updated.getName()),
          Map.class);

      OutboxEvent event = new OutboxEvent();
      event.setId(UUID.randomUUID());
      event.setAggregateType("Stock");
      event.setAggregateId(updated.getId().toString());
      event.setType("stock-updated");
      event.setPayload(payload);
      event.setCreatedAt(Instant.now());
      log.info("Outbox event created: {}", event.getId());

      return updated;

    } catch (ObjectOptimisticLockingFailureException ex) {
      throw new RuntimeException("Concurrency conflict detected (optimistic lock)");
    }
  }
}
