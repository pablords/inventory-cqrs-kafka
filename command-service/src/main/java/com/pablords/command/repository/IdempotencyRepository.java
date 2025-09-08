package com.pablords.command.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pablords.command.model.IdempotencyKey;

public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, UUID> {

  @Modifying
  @Query(value = """
      INSERT INTO idempotency_keys (key, request_hash, status, response_body, created_at, updated_at)
      VALUES (:key, :hash, 'IN_PROGRESS', '{}'::jsonb, now(), now())
      ON CONFLICT (key) DO NOTHING
      """, nativeQuery = true)
  int tryInsert(@Param("key") UUID key, @Param("hash") String hash);

  Optional<IdempotencyKey> findById(UUID key);
}
