package com.pablords.shared.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class KafkaCommons {
  @Bean
  @Qualifier("retryTemplate")
  public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();

    // Configura o número de tentativas e o intervalo entre elas
    FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(1000L); // 1 segundo entre as tentativas
    retryTemplate.setBackOffPolicy(backOffPolicy);

    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(3); // Máximo de 3 tentativas
    retryTemplate.setRetryPolicy(retryPolicy);

    return retryTemplate;
  }
}
