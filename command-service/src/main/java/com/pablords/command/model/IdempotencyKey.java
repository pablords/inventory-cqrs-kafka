package com.pablords.command.model;

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
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "idempotency_keys", indexes = {
		@Index(name = "idx_idem_updated_at", columnList = "updated_at")
})
@Data
public class IdempotencyKey {

	@Id
	@Column(name = "key", nullable = false, updatable = false)
	private UUID key; // <<< UUID como PK

	@Column(name = "request_hash", nullable = false, length = 64)
	private String requestHash; // camelCase no Java, snake_case no DB

	@Column(name = "status", nullable = false, length = 20)
	private String status; // IN_PROGRESS | SUCCEEDED | FAILED

	@Lob
	@Column(name = "response_body", columnDefinition = "jsonb", nullable = false)
	@Type(JsonBinaryType.class) // hypersistence-utils
	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, Object> responseBody;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		if (createdAt == null)
			createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}
}