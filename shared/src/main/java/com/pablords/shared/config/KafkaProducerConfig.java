package com.pablords.shared.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.pablords.shared.events.StockUpdatedEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducerConfig.class);

  @Bean
  public ProducerFactory<String, StockUpdatedEvent> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true); // Inclui o cabeçalho __TypeId__
    configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Garante idempotência
    configProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "stock-updated-transactional-id"); // Necessário para
                                                                                               // transações
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  @Qualifier("stockUpdatedKafkaTemplate")
  public KafkaTemplate<String, StockUpdatedEvent> kafkaTemplate() {
    log.info("Creating KafkaTemplate for StockUpdatedEvent");
    KafkaTemplate<String, StockUpdatedEvent> kafkaTemplate = new KafkaTemplate<>(producerFactory());
    return kafkaTemplate;
  }

}