package com.pablords.inventory.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pablords.inventory.model.Outbox;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
}

