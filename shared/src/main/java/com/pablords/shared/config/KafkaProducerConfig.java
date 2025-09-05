package com.pablords.shared.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.pablords.shared.events.StockUpdatedEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaProducerConfig {

  // Producer do fluxo principal (transacional opcional, com PREFIXO)
  @Bean
  public ProducerFactory<String, StockUpdatedEvent> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
    configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    // NÃO defina TRANSACTIONAL_ID_CONFIG fixo; use prefixo:
    DefaultKafkaProducerFactory<String, StockUpdatedEvent> pf =
        new DefaultKafkaProducerFactory<>(configProps);
    pf.setTransactionIdPrefix("stock-updated-tx-");
    return pf;
  }

  @Bean
  @Qualifier("stockUpdatedKafkaTemplate")
  public KafkaTemplate<String, StockUpdatedEvent> kafkaTemplate(
      ProducerFactory<String, StockUpdatedEvent> pf) {
    log.info("Creating KafkaTemplate for StockUpdatedEvent");
    return new KafkaTemplate<>(pf);
  }

  // DLQ: producer NÃO-transacional e genérico
  @Bean
  public ProducerFactory<Object, Object> dlqProducerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  @Qualifier("dlqTemplate")
  public KafkaTemplate<Object, Object> dlqTemplate(
      ProducerFactory<Object, Object> pf) {
    return new KafkaTemplate<>(pf);
  }
}
