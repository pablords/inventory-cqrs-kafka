package com.pablords.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pablords.command.model.Order;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
