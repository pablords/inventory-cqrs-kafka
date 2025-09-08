package com.pablords.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablords.command.dto.request.CreateOrderDTO;
import com.pablords.command.dto.request.OrderItemCreateDTO;
import com.pablords.command.model.Order;
import com.pablords.command.model.OrderItem;
import com.pablords.command.service.OrderTransactionalOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
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
    private List<OrderItem> items;
    private List<OrderItemCreateDTO> productsDto = List.of(
            new OrderItemCreateDTO(UUID.randomUUID(), 3),
            new OrderItemCreateDTO(UUID.randomUUID(), 2));
    private Order order;
    private String requestId;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID().toString();
        items = List.of(
                new OrderItem(),
                new OrderItem()
        );


        order = new Order();
        order.setItems(items);
        order.setId(UUID.randomUUID());
        order.setStatus("COMPLETED");
    }

    @Test
    void createOrder_success() throws Exception {
        Mockito.when(orchestrator.processOrder(any(), any(String.class))).thenReturn(order);
        CreateOrderDTO req = new CreateOrderDTO(productsDto, requestId);
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(order.getId().toString()))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void createOrder_invalidQuantity() throws Exception {
        CreateOrderDTO req = new CreateOrderDTO(productsDto, requestId);
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }
}
