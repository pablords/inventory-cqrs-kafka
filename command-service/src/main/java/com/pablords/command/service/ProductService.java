package com.pablords.command.service;

import com.pablords.command.exception.BusinessException;
import com.pablords.command.exception.ErrorMessages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablords.shared.events.StockUpdatedEvent;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import com.pablords.command.model.Outbox;
import com.pablords.command.model.Product;
import com.pablords.command.repository.OutboxRepository;
import com.pablords.command.repository.ProductRepository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ProductService {

  private final ProductRepository productRepository;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ProductService(ProductRepository productRepository, OutboxRepository outboxRepository) {
    this.productRepository = productRepository;
    this.outboxRepository = outboxRepository;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Product removeStock(UUID productId, int amount) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new BusinessException(ErrorMessages.PRODUCT_NOT_FOUND.getMessage()));

    try {
      product.removeStock(amount);
      Product updated = productRepository.save(product);
      saveOutbox(updated);
      return updated;
    } catch (RuntimeException e) {
      throw new BusinessException(e.getMessage());
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Product addStock(UUID productId, int amount) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new BusinessException(ErrorMessages.PRODUCT_NOT_FOUND.getMessage()));

    try {
      product.addStock(amount);
      Product updated = productRepository.save(product);
      saveOutbox(updated);
      return updated;
    } catch (RuntimeException e) {
      throw new BusinessException(e.getMessage());
    }

  }

  @SuppressWarnings("unchecked")
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

  public Product createProduct(String name, int initialQty) {
    Product product = new Product();
    product.setName(name);
    product.setQuantity(initialQty);
    Product savedProduct = productRepository.save(product);
    saveOutbox(savedProduct);
    return savedProduct;
  }

  public Product findById(UUID productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new BusinessException(ErrorMessages.PRODUCT_NOT_FOUND.getMessage()));
  }
}
