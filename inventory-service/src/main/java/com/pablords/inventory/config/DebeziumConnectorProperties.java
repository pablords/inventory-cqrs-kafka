package com.pablords.inventory.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "debezium.connector")
public class DebeziumConnectorProperties {
    private Map<String, String> config;
}
