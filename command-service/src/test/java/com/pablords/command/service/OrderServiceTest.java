package com.pablords.command.service;

import com.pablords.command.dto.request.OrderItemCreateDTO;
import com.pablords.command.model.Order;
import com.pablords.command.model.OrderItem;
import com.pablords.command.model.Product;
import com.pablords.command.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private OrderService orderService;

    private List<OrderItemCreateDTO> productsDto;
    private List<OrderItem> items = List.of(
            new OrderItem(),
            new OrderItem()
    );
    private String requestId = "req-123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productsDto = List.of(
                new OrderItemCreateDTO(UUID.randomUUID(), 2),
                new OrderItemCreateDTO(UUID.randomUUID(), 1)
        );
    }

    @Test
    void createOrder_success() {
        Order order = new Order();
        order.setItems(items);
        order.setId(UUID.randomUUID());
        order.setStatus("COMPLETED");
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        Order result = orderService.createOrder(items);
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }
}
