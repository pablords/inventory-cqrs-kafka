package com.pablords.command.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pablords.command.model.Order;
import com.pablords.command.dto.request.CreateOrderDTO;
import com.pablords.command.dto.response.OrderDTO;
import com.pablords.command.service.OrderTransactionalOrchestrator;

import jakarta.validation.Valid;
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
  public ResponseEntity<OrderDTO> create(@Valid @RequestBody CreateOrderDTO request,
      @RequestHeader(value = "Idempotency-Key", required = false) String idemKeyHeader) {
    String idemKey = (idemKeyHeader == null || idemKeyHeader.isBlank())
        ? UUID.randomUUID().toString() // fallback elegante
        : idemKeyHeader;
    log.info("Creating order with items: {} and idemKeyHeader: {}", request.items(), idemKey);
    Order order = orderTransactionalOrchestrator.processOrder(request, idemKey);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .header("Idempotency-Key", idemKey) // devolve para o cliente reutilizar em retries
        .body(OrderDTO.fromEntity(order));
  }
}
