package com.pablords.command.service;

import com.pablords.command.model.Order;
import com.pablords.command.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private OrderService orderService;

    private UUID productId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productId = UUID.randomUUID();
    }

    @Test
    void createOrder_success() {
        Order order = new Order(productId, 3);
        order.setId(UUID.randomUUID());
        order.setStatus("COMPLETED");
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        Order result = orderService.createOrder(productId, 3);
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(3, result.getQuantity());
        assertEquals("COMPLETED", result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }
}
