package com.pablords.command.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pablords.command.model.Outbox;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
}

