package com.pablords.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pablords.inventory.model.Order;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
