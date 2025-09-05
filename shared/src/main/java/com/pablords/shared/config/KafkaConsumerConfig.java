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
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // ajuste p/ container se necessário
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "query-service");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    // props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // se quiser começar do início

    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
    props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.pablords.shared.events,*");
    props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // NÃO usa __TypeId__; usa VALUE_DEFAULT_TYPE
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, StockUpdatedEvent.class);

    return new DefaultKafkaConsumerFactory<>(props);
  }

  // Recoverer para o TÓPICO PRINCIPAL (publica na DLQ usando template não-transacional)
  @Bean
  public DeadLetterPublishingRecoverer dlqRecoverer(
      @Qualifier("dlqTemplate") KafkaTemplate<Object, Object> dlqTemplate) {

    return new DeadLetterPublishingRecoverer(dlqTemplate, (rec, ex) -> {
      if (rec.topic().endsWith("-dlq")) {
        log.error("Message from DLQ failed; skip re-DLQ. topic={}, partition={}, offset={}",
            rec.topic(), rec.partition(), rec.offset());
        return null;
      }
      log.error("Publishing to DLQ. topic={}, partition={}, offset={}, error={}",
          rec.topic(), rec.partition(), rec.offset(), ex.getMessage());
      return new TopicPartition(rec.topic() + "-dlq", rec.partition());
    });
  }

  @Bean
  public DefaultErrorHandler mainErrorHandler(DeadLetterPublishingRecoverer dlqRecoverer) {
    DefaultErrorHandler eh = new DefaultErrorHandler(dlqRecoverer, new FixedBackOff(1000L, 1));
    eh.setCommitRecovered(true); // <- confirma o offset após publicar na DLQ
    return eh;
  }

  // Factory do tópico PRINCIPAL (com handler/DLQ)
  @Bean(name = "kafkaListenerContainerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, StockUpdatedEvent> mainFactory(
      ConsumerFactory<String, StockUpdatedEvent> cf,
      DefaultErrorHandler mainErrorHandler) {

    var factory = new ConcurrentKafkaListenerContainerFactory<String, StockUpdatedEvent>();
    factory.setConsumerFactory(cf);
    factory.setCommonErrorHandler(mainErrorHandler);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    factory.getContainerProperties().setMissingTopicsFatal(true);
    return factory;
  }

  // Factory para consumir a DLQ (sem reenviar para outra DLQ)
  @Bean(name = "kafkaDlqListenerContainerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, StockUpdatedEvent> dlqFactory(
      ConsumerFactory<String, StockUpdatedEvent> cf) {

    var factory = new ConcurrentKafkaListenerContainerFactory<String, StockUpdatedEvent>();
    factory.setConsumerFactory(cf);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    return factory;
  }
}
