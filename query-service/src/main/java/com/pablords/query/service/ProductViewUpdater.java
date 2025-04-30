package com.pablords.query.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.pablords.shared.events.StockUpdatedEvent;


import com.pablords.query.model.ProductView;
import com.pablords.query.repository.ProductViewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductViewUpdater {

  private final ProductViewRepository productViewRepository;
  private static final Logger log = LoggerFactory.getLogger(ProductViewUpdater.class);

  public ProductViewUpdater(ProductViewRepository productViewRepository) {
    this.productViewRepository = productViewRepository;
  }

  @KafkaListener(topics = "stock-updated", groupId = "query-service", containerFactory = "kafkaListenerContainerFactory")
  public void onStockUpdated(StockUpdatedEvent event) {
      log.info("Received stock updated event: {}", event.getProductId());
      ProductView view = productViewRepository.findById(event.getProductId())
          .orElse(new ProductView(event.getProductId(), event.getName(), 0));
  
      view.setQuantity(event.getNewQuantity());
      productViewRepository.save(view);
  }
}
