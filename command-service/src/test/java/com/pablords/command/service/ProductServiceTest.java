package com.pablords.command.service;

import com.pablords.command.exception.BusinessException;
import com.pablords.command.model.Product;
import com.pablords.command.repository.OutboxRepository;
import com.pablords.command.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private OutboxRepository outboxRepository;
    @InjectMocks
    private ProductService productService;

    private Product product;
    private UUID productId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productId = UUID.randomUUID();
        product = new Product("Test", 10);
        product.setId(productId);
    }

    @Test
    void addStock_success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        var updated = productService.addStock(productId, 5);
        assertEquals(15, updated.getQuantity());
        verify(outboxRepository).save(any());
    }

    @Test
    void removeStock_success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        var updated = productService.removeStock(productId, 5);
        assertEquals(5, updated.getQuantity());
        verify(outboxRepository).save(any());
    }

    @Test
    void removeStock_insufficient() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        assertThrows(BusinessException.class, () -> productService.removeStock(productId, 20));
    }

    @Test
    void addStock_productNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> productService.addStock(productId, 5));
    }
}
