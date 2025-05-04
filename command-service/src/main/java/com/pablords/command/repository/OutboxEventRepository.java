package com.pablords.command.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pablords.command.domain.OutboxEvent;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
}

