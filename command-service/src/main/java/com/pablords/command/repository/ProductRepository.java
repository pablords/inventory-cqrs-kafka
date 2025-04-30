package com.pablords.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pablords.command.domain.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
}

