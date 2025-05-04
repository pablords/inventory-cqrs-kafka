package com.pablords.command.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DebeziumConnectorRegistrar {

  private static final Logger log = LoggerFactory.getLogger(DebeziumConnectorRegistrar.class);
  private final RestTemplate restTemplate;

  public DebeziumConnectorRegistrar(RestTemplateBuilder builder) {
    this.restTemplate = builder
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(10))
        .build();
  }

  @PostConstruct
  public void registerConnector() {
    String connectorName = "inventory-postgres-connector";
    String connectUrl = "http://localhost:8083/connectors";

    // Verifica se o conector já está registrado
    try {
      ResponseEntity<String> getResponse = restTemplate.getForEntity(connectUrl + "/" + connectorName, String.class);
      if (getResponse.getStatusCode().is2xxSuccessful()) {
        log.info("Conector '{}' já registrado no Kafka Connect.", connectorName);
        return;
      }
    } catch (Exception e) {
      log.info("Conector '{}' ainda não está registrado. Registrando agora...", connectorName);
    }

    // Monta o corpo da requisição
    Map<String, Object> config = new HashMap<>();
    config.put("connector.class", "io.debezium.connector.postgresql.PostgresConnector");
    config.put("database.hostname", "db-command");
    config.put("database.port", "5432");
    config.put("database.user", "user");
    config.put("database.password", "pass");
    config.put("database.dbname", "inventory");
    config.put("database.server.name", "postgres-inventory");
    config.put("plugin.name", "pgoutput");
    config.put("slot.name", "outbox_slot");
    config.put("table.include.list", "public.outbox_event");
    config.put("key.converter", "org.apache.kafka.connect.json.JsonConverter");
    config.put("value.converter", "org.apache.kafka.connect.storage.StringConverter");
    config.put("value.converter.schemas.enable", "false");
    config.put("key.converter.schemas.enable", "false");
    config.put("transforms.outbox.unwrap.payload", "true");
   

    // Configuração do EventRouter do Outbox Pattern
    config.put("transforms", "outbox");
    config.put("transforms.outbox.type", "io.debezium.transforms.outbox.EventRouter");
    config.put("transforms.outbox.table.field.event.id", "id");
    config.put("transforms.outbox.table.field.event.key", "aggregate_id");
    config.put("transforms.outbox.table.field.event.payload", "payload");
    config.put("transforms.outbox.route.by.field", "type"); // Certifique-se de que o nome do campo está correto
 
    // Adiciona o prefixo obrigatório para os tópicos
    config.put("topic.prefix", ".");

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("name", connectorName);
    requestBody.put("config", config);

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
