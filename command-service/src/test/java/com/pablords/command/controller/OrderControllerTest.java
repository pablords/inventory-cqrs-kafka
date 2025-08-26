package com.pablords.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablords.command.dto.CreateOrderRequest;
import com.pablords.command.dto.OrderDTO;
import com.pablords.command.model.Order;
import com.pablords.command.service.OrderTransactionalOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderTransactionalOrchestrator orchestrator;

    private ObjectMapper objectMapper = new ObjectMapper();
    private UUID productId;
    private Order order;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        order = new Order(productId, 5);
        order.setId(UUID.randomUUID());
        order.setStatus("COMPLETED");
    }

    @Test
    void createOrder_success() throws Exception {
        Mockito.when(orchestrator.processOrder(any(UUID.class), any(Integer.class))).thenReturn(order);
        CreateOrderRequest req = new CreateOrderRequest(productId, 5);
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(order.getId().toString()))
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void createOrder_invalidQuantity() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest(productId, 0);
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }
}
