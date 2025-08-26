package com.pablords.command.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class DebeziumConnectorRegistry {

  @Value("${debezium.connector.name}")
  private String connectorName;
  private final RestTemplate restTemplate;
  private final DebeziumConnectorProperties properties;

  public DebeziumConnectorRegistry(RestTemplateBuilder builder, DebeziumConnectorProperties properties) {
    this.restTemplate = builder
        .connectTimeout(Duration.ofSeconds(5))
        .readTimeout(Duration.ofSeconds(10))
        .build();
    this.properties = properties;
  }

  @PostConstruct
  public void registerConnector() {
    String connectUrl = "http://localhost:8083/connectors";

    try {
      ResponseEntity<String> getResponse = restTemplate.getForEntity(connectUrl + "/" + connectorName, String.class);
      if (getResponse.getStatusCode().is2xxSuccessful()) {
        log.info("Conector '{}' já registrado no Kafka Connect.", connectorName);
        return;
      }
    } catch (Exception e) {
      log.info("Conector '{}' ainda não está registrado. Registrando agora...", connectorName);
    }

    log.info("Configuração enviada ao Kafka Connect: {}", properties.getConfig());

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("name", connectorName);
    requestBody.put("config", properties.getConfig());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(connectUrl, request, String.class);
      log.info("Debezium Connector registrado com sucesso: {}", response.getBody());
    } catch (Exception e) {
      log.error("Erro ao registrar o Debezium Connector: {}", e.getMessage(), e);
    }
  }
}
