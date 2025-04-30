package com.pablords.command.service;


import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pablords.command.domain.Product;
import com.pablords.command.domain.StockUpdatedEvent;
import com.pablords.command.repository.ProductRepository;

import org.springframework.kafka.core.KafkaTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductCommandService {

  private static final Logger log = LoggerFactory.getLogger(ProductCommandService.class);

  private final ProductRepository repository;
  private final KafkaTemplate<String, StockUpdatedEvent> kafkaTemplate;

  public ProductCommandService(ProductRepository repository, KafkaTemplate<String, StockUpdatedEvent> kafkaTemplate) {
    this.repository = repository;
    this.kafkaTemplate = kafkaTemplate;
  }

  @Transactional
  public Product createProduct(String name, int initialQty) {
    Product product = new Product(name, initialQty);
    Product saved = repository.save(product);

    // Publica o evento no Kafka
    kafkaTemplate.send("stock-updated",
        new StockUpdatedEvent(saved.getId(), saved.getQuantity(), saved.getName()));

    return saved;
  }

  @Transactional
  public Product addStock(String productId, int amount) {
    try {
      Product product = repository.findById(productId)
          .orElseThrow(() -> new RuntimeException("Product not found"));

      product.addStock(amount);
      Product updated = repository.save(product);
      log.info("Product updated: {}", updated.getId());
      // Publica o evento no Kafka
      kafkaTemplate.send("stock-updated",
          new StockUpdatedEvent(updated.getId(), updated.getQuantity(), updated.getName()));
      return updated;

    } catch (ObjectOptimisticLockingFailureException ex) {
      // Este é o caso do Optimistic Lock falhar
      // Ex.: outro processo atualizou o registro primeiro.
      throw new RuntimeException("Concurrency conflict detected (optimistic lock)");
    }
  }

  @Transactional
  public Product removeStock(String productId, int amount) {
    try {
      Product product = repository.findById(productId)
          .orElseThrow(() -> new RuntimeException("Product not found"));

      product.removeStock(amount);
      Product updated = repository.save(product);
      // Publica o evento no Kafka
      kafkaTemplate.send("stock-updated",
          new StockUpdatedEvent(updated.getId(), updated.getQuantity(), updated.getName()));
      return updated;

    } catch (ObjectOptimisticLockingFailureException ex) {
      throw new RuntimeException("Concurrency conflict detected (optimistic lock)");
    }
  }
}
