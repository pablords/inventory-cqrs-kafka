package com.pablords.inventory.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pablords.inventory.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}

