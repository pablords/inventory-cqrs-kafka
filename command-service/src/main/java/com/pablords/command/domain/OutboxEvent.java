package com.pablords.command.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

  @Id
  private UUID id;

  private String aggregateType;
  private String aggregateId;
  private String type;
  private Instant createdAt;

  @Lob
  @Column(columnDefinition = "jsonb", nullable = false)
  @Type(JsonBinaryType.class)
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> payload;



  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public void setAggregateType(String aggregateType) {
    this.aggregateType = aggregateType;
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public void setAggregateId(String aggregateId) {
    this.aggregateId = aggregateId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
  public Map<String, Object> getPayload() {
    return payload;
  }
  public void setPayload(Map<String, Object> payload) {
    this.payload = payload;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

}
