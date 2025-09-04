package com.pablords.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablords.command.dto.response.ProductDTO;
import com.pablords.command.model.Product;
import com.pablords.command.service.ProductService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private ObjectMapper objectMapper = new ObjectMapper();
    private UUID productId;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = new Product("Test", 10);
        product.setId(productId);
    }

    @Test
    void addStock_returnsProductDTO() throws Exception {
        Mockito.when(productService.addStock(eq(productId), eq(5))).thenReturn(product);
        mockMvc.perform(post("/products/" + productId + "/add")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("amount", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void create_returnsProductDTO() throws Exception {
        Mockito.when(productService.createProduct(eq("Test"), eq(10))).thenReturn(product);
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test")
                .param("initialQty", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test"));
    }
}
