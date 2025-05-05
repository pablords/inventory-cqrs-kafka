package com.pablords.command.service;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablords.shared.events.StockUpdatedEvent;

import lombok.extern.slf4j.Slf4j;

import com.pablords.command.model.Outbox;
import com.pablords.command.model.Product;
import com.pablords.command.repository.OutboxRepository;
import com.pablords.command.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ProductService {

  private final ProductRepository productRepository;
  private final OutboxRepository outboxRepository;
  private final KafkaTemplate<String, StockUpdatedEvent> kafkaTemplate;
  final ObjectMapper objectMapper = new ObjectMapper();

  public ProductService(ProductRepository productRepository,
      @Qualifier("stockUpdatedKafkaTemplate") KafkaTemplate<String, StockUpdatedEvent> kafkaTemplate,
      OutboxRepository outboxRepository) {
    this.productRepository = productRepository;
    this.kafkaTemplate = kafkaTemplate;
    this.outboxRepository = outboxRepository;
  }

  @Transactional
  public Product createProduct(String name, int initialQty) {
    try {
      Product product = new Product(name, initialQty);
      Product saved = productRepository.save(product);

      log.info("Product updated: {}", saved.getId());
      this.saveOutbox(saved);

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
      this.saveOutbox(updated);
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
      log.info("Product updated: {}", updated.getId());
      this.saveOutbox(updated);
      return updated;
    } catch (ObjectOptimisticLockingFailureException ex) {
      throw new RuntimeException("Concurrency conflict detected (optimistic lock)");
    }
  }

  private void saveOutbox(Product updatedProduct) {
    log.info("Saving outbox event for product: {}", updatedProduct.getId());
    Map<String, Object> payload = objectMapper.convertValue(
        new StockUpdatedEvent(updatedProduct.getId().toString(), updatedProduct.getQuantity(),
            updatedProduct.getName()),
        Map.class);
    Outbox event = new Outbox();
    event.setId(UUID.randomUUID());
    event.setAggregateType("Stock");
    event.setAggregateId(updatedProduct.getId().toString());
    event.setType("stock-updated");
    event.setPayload(payload);
    event.setCreatedAt(Instant.now());
    outboxRepository.save(event);
    log.info("Outbox event created: {}", event.getId());
  }

}
