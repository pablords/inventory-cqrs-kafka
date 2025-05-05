package com.pablords.query.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pablords.shared.events.StockUpdatedEvent;

import lombok.extern.slf4j.Slf4j;

import com.pablords.query.model.ProductView;
import com.pablords.query.repository.ProductViewRepository;


@Service
@Slf4j
public class ProductViewUpdater {

  private final ProductViewRepository productViewRepository;
  private RetryTemplate retryTemplate;

  public ProductViewUpdater(ProductViewRepository productViewRepository
    ) {
    this.productViewRepository = productViewRepository;
  }

  @Transactional
  @KafkaListener(topics = "outbox.event.stock-updated", groupId = "query-service", containerFactory = "kafkaListenerContainerFactory", concurrency = "3")
  public void onStockUpdated(StockUpdatedEvent event, Acknowledgment acknowledgment) {
    log.info("Received stock updated event id: {}, amount: {}", event.getProductId(), event.getNewQuantity());
    try {
      // if (event.getName().equals("Product A")) {
      //   throw new RuntimeException("Simulated error");
      // }
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

  @KafkaListener(topics = "outbox.event.stock-updated-dlq", groupId = "dlq-query-service", containerFactory = "kafkaListenerContainerFactory")
  public void processDLQMessage(StockUpdatedEvent event,
      Acknowledgment acknowledgment) {
    try {
      retryTemplate.execute(context -> {
        log.info("Retrying message from DLQ: {}, count: {}", event.getProductId(), context.getRetryCount());
        // Tente processar a mensagem novamente
        ProductView view = productViewRepository.findById(event.getProductId())
            .orElse(new ProductView(event.getProductId(), event.getName(), 0, "stock-updated-dlq"));

        view.setQuantity(event.getNewQuantity());
        view.setTopic("stock-updated-dlq");
     
        view.setLog(context.getLastThrowable().getMessage());

        productViewRepository.save(view);

        // Confirma a mensagem manualmente após o processamento bem-sucedido
        acknowledgment.acknowledge();
        return null;
      });
    } catch (Exception e) {
      log.error("Failed to process message from DLQ after retries: {}", event, e);
      // Decida o que fazer com mensagens que falham após todas as tentativas
    }

  }
}
