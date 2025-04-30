package com.pablords.command.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private UUID id;
    private String name;
    private Integer quantity;

    @Version
    private Long version;  // usado para Optimistic Locking

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() { }

    public Product(String name, int quantity) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addStock(int amount) {
        this.quantity += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void removeStock(int amount) {
        if (this.quantity < amount) {
            throw new RuntimeException("Not enough stock");
        }
        this.quantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
      return id;
    }

    public void setId(UUID id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Integer getQuantity() {
      return quantity;
    }

    public void setQuantity(Integer quantity) {
      this.quantity = quantity;
    }

    public Long getVersion() {
      return version;
    }

    public void setVersion(Long version) {
      this.version = version;
    }

    public LocalDateTime getCreatedAt() {
      return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
      return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
    }
}

