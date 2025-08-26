package com.pablords.query.controller;


import com.pablords.query.model.ProductView;
import com.pablords.query.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@WebMvcTest
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private ProductView productView;

    @BeforeEach
    void setUp() {
        productView = new ProductView("1", "Test", 10, "topic");
    }

    @Test
    void findAll_success() throws Exception {
        Page<ProductView> page = new PageImpl<>(List.of(productView), PageRequest.of(0, 10), 1);
        Mockito.when(productService.findAll(any(PageRequest.class))).thenReturn(page);
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.content[0].name").value("Test"));
    }

    @Test
    void findById_success() throws Exception {
        Mockito.when(productService.findById("1")).thenReturn(productView);
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void findById_notFound() throws Exception {
        Mockito.when(productService.findById(anyString())).thenThrow(new com.pablords.query.exception.NotFoundException("not found"));
        mockMvc.perform(get("/products/2"))
                .andExpect(status().is4xxClientError());
    }
}
