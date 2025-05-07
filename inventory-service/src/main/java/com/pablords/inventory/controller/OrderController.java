package com.pablords.inventory.controller;

import java.util.UUID;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pablords.inventory.model.Order;
import com.pablords.inventory.service.OrderTransactionalOrchestrator;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {
  private final OrderTransactionalOrchestrator orderTransactionalOrchestrator;

  public OrderController(OrderTransactionalOrchestrator orderTransactionalOrchestrator) {
    this.orderTransactionalOrchestrator = orderTransactionalOrchestrator;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Order create(@RequestParam String productId, @RequestParam int quantity) {
    log.info("Creating order with productId: {} and quantity: {}", productId, quantity);
    return orderTransactionalOrchestrator.processOrder(UUID.fromString(productId), quantity);
  }
}
