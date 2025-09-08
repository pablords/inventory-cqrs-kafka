package com.pablords.command.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

import com.pablords.command.model.Order;
import com.pablords.command.model.OrderItem;
import com.pablords.command.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;

  public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  public Order createOrder(List<OrderItem> items) {
    Order order = new Order();
    order.setStatus("COMPLETED");
    order.setItems(items);
    return orderRepository.save(order);
  }


}
