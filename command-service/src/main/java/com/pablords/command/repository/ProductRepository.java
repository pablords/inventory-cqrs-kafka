package com.pablords.command.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pablords.command.domain.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}

