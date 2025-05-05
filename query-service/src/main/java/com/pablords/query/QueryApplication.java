package com.pablords.query;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Import;

import lombok.extern.slf4j.Slf4j;


@SpringBootApplication(scanBasePackages = "com.pablords.query")
@Import({
    com.pablords.shared.config.KafkaConsumerConfig.class,
    com.pablords.shared.config.KafkaProducerConfig.class
})
@Slf4j
public class QueryApplication implements CommandLineRunner {


  @Value("${spring.application.name}")
  String appName;

  @Override
  public void run(String... args) {
    log.info("{} app is running", appName);
  }

  public static void main(String[] args) {
    SpringApplication.run(QueryApplication.class, args);
  }

}
