package com.pablords.command.service;

import java.util.UUID;
import org.springframework.stereotype.Service;

import com.pablords.command.model.Order;
import com.pablords.command.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;

  public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  public Order createOrder(UUID productId, int quantity) {
    Order order = new Order(productId, quantity);
    order.setStatus("COMPLETED");
    return orderRepository.save(order);
  }
}
