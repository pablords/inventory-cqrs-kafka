package com.pablords.shared.config;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import com.pablords.shared.events.StockUpdatedEvent;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  @Bean
  public ConsumerFactory<String, StockUpdatedEvent> consumerFactory() {
      Map<String, Object> props = new HashMap<>();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "query-service");
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
      props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
      props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
      props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.pablords.shared.events,*"); // Permite desserializar qualquer pacote confiável
      props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Inclui o cabeçalho __TypeId__
      props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, StockUpdatedEvent.class); // Define o tipo padrão
  
      return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, StockUpdatedEvent> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, StockUpdatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    // Configura um DefaultErrorHandler com backoff fixo
    factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3))); // 1 segundo de intervalo, 3
                                                                                        // tentativas

    return factory;
  }
}