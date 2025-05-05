package com.pablords.shared.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import com.pablords.shared.events.StockUpdatedEvent;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@Slf4j
public class KafkaConsumerConfig {


  @Bean
  public ConsumerFactory<String, StockUpdatedEvent> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "query-service");
    props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"); // Garante que apenas mensagens confirmadas
                                                                        // sejam lidas
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
    props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.pablords.shared.events,*"); // Permite desserializar qualquer
                                                                                  // pacote confiável
    props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Inclui o cabeçalho __TypeId__
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, StockUpdatedEvent.class); // Define o tipo padrão
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, StockUpdatedEvent> kafkaListenerContainerFactory(
      @Qualifier("stockUpdatedKafkaTemplate") KafkaTemplate<String, StockUpdatedEvent> kafkaTemplate
     ) {
    log.info("Using KafkaTemplate: {}", kafkaTemplate);
    ConcurrentKafkaListenerContainerFactory<String, StockUpdatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    // Configura o DeadLetterPublishingRecoverer
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
        (record, ex) -> {
          // Evita enviar mensagens do DLQ para outro DLQ
          if (record.topic().endsWith("-dlq")) {
            log.error("Message from DLQ failed. Not sending to another DLQ. Topic: {}, Partition: {}, Offset: {}",
                record.topic(), record.partition(), record.offset());
            return null; // Não envie para outro DLQ
          }
          log.error("Publishing to DLQ. Topic: {}, Partition: {}, Offset: {}, Error: {}",
              record.topic(), record.partition(), record.offset(), ex.getMessage());
          return new TopicPartition(record.topic() + "-dlq", record.partition());
        });

    // Configura o DefaultErrorHandler com o recoverer
    DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 1));
    errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
      log.error("Failed to process message after {} attempts. Topic: {}, Partition: {}, Offset: {}, Error: {}",
          deliveryAttempt, record.topic(), record.partition(), record.offset(), ex.getMessage());
    });

    factory.setCommonErrorHandler(errorHandler);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    return factory;
  }

}