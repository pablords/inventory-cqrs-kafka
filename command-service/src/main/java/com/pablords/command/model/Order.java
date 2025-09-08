package com.pablords.command.model;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "orders")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<OrderItem> items = new ArrayList<>();

  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private BigInteger quantity;

  @Version
  private Long version;



  public Order() {
    this.status = "PENDING";
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    recomputeQuantity();
  }

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
    recomputeQuantity();
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
    recomputeQuantity();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }


  public List<OrderItem> getItems() {
    return items;
  }

  public void setItems(List<OrderItem> items) {
    this.items.clear();
    if (items != null) {
      for (OrderItem it : items) {
        this.items.add(it);
        it.setOrder(this); // >>> seta o lado dono
      }
    }
  }

  public BigInteger getQuantity() {
    return quantity;
  }

  public void recomputeQuantity() {
    int sum = (items == null ? 0 : items.stream().mapToInt(OrderItem::getQuantity).sum());
    this.quantity = BigInteger.valueOf(sum);
  }

  public void addItem(OrderItem item) {
    if (item == null)
      return;
    this.items.add(item);
    item.setOrder(this);
    recomputeQuantity();
  }

  public void removeItem(OrderItem item) {
    if (item == null)
      return;
    this.items.remove(item);
    item.setOrder(null);
    recomputeQuantity();
  }

}
