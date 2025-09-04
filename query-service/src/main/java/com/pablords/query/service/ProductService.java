package com.pablords.query.service;


import com.pablords.query.exception.NotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pablords.shared.events.StockUpdatedEvent;

import lombok.extern.slf4j.Slf4j;

import com.pablords.query.model.ProductView;
import com.pablords.query.repository.ProductViewRepository;

@Service
@Slf4j
public class ProductService {

  private final ProductViewRepository productViewRepository;

  public ProductService(ProductViewRepository productViewRepository) {
    this.productViewRepository = productViewRepository;
  }

  @Transactional
  @KafkaListener(topics = "${consumer.topic.name}", groupId = "${consumer.group-id}", containerFactory = "kafkaListenerContainerFactory", concurrency = "3")
  public void onStockUpdated(StockUpdatedEvent event, Acknowledgment acknowledgment) {
    log.info("Received stock updated event id: {}, amount: {}", event.getProductId(), event.getNewQuantity());
    try {
      // Simulação de erro para teste
      // Descomente a linha abaixo para simular uma falha
      if (event.getName().equals("Product A")) {
        throw new RuntimeException("Simulated error");
      }
      ProductView view = productViewRepository.findById(event.getProductId())
          .orElse(new ProductView(event.getProductId(), event.getName(), 0, "stock-updated"));

      view.setQuantity(event.getNewQuantity());
      view.setTopic("stock-updated");
      view.setLog("");
      productViewRepository.save(view);

      // Confirma a mensagem manualmente após o processamento bem-sucedido
      acknowledgment.acknowledge();
      log.info("Message processed successfully for product: {}", event.getProductId());
    } catch (Exception e) {
      log.error("Error processing stock updated event: {}", event, e);
      throw e; // Repropaga a exceção para que o DefaultErrorHandler seja acionado
    }
  }

  @Transactional
  @KafkaListener(topics = "${consumer.topic.name}-dlq", groupId = "dlq-${consumer.group-id}", containerFactory = "kafkaListenerContainerFactory", concurrency = "3")
  public void consumeDLQMessage(StockUpdatedEvent event,
      Acknowledgment acknowledgment) {
    try {
      log.info("Retrying message from DLQ: {}", event.getProductId());

      ProductView view = productViewRepository.findById(event.getProductId())
          .orElse(new ProductView(event.getProductId(), event.getName(), 0, "stock-updated-dlq"));

      view.setQuantity(event.getNewQuantity());
      view.setTopic("stock-updated-dlq");

      productViewRepository.save(view);

      acknowledgment.acknowledge();
      log.info("Message processed successfully from DLQ: {}", event.getProductId());
    } catch (Exception e) {
      log.error("Failed to process message from DLQ after retries: {}", event, e);
      // Decida o que fazer com mensagens que falham após todas as tentativas
    }

  }

  public ProductView findById(String id) {
  return productViewRepository.findById(id)
    .orElseThrow(() -> new NotFoundException("Product not found in read model"));
  }

  public Page<ProductView> findAll(Pageable pageable) {
    return productViewRepository.findAll(pageable);
  }
}
