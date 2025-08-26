package com.pablords.command.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pablords.command.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}

