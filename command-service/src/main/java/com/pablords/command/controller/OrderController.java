package com.pablords.command.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.pablords.command.dto.CreateOrderRequest;
import com.pablords.command.exception.BusinessException;
import com.pablords.command.model.Order;
import com.pablords.command.dto.OrderDTO;
import com.pablords.command.service.OrderTransactionalOrchestrator;

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
  public ResponseEntity<OrderDTO> create(@RequestBody CreateOrderRequest request) {
    if (request.quantity() <= 0) {
      throw new BusinessException("Quantity must be greater than 0");
    }
    log.info("Creating order with productId: {} and quantity: {}", request.productId(), request.quantity());
    Order order = orderTransactionalOrchestrator.processOrder(request.productId(), request.quantity());
    return ResponseEntity.status(HttpStatus.CREATED).body(OrderDTO.fromEntity(order));
  }
}
