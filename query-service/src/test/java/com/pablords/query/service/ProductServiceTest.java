package com.pablords.query.service;

import com.pablords.query.exception.NotFoundException;
import com.pablords.query.model.ProductView;
import com.pablords.query.repository.ProductViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
// import removido: org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductViewRepository productViewRepository;
    @InjectMocks
    private ProductService productService;

    private ProductView productView;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productView = new ProductView("1", "Test", 10, "topic");
    }

    @Test
    void findById_success() {
        when(productViewRepository.findById("1")).thenReturn(Optional.of(productView));
        ProductView result = productService.findById("1");
        assertNotNull(result);
        assertEquals("Test", result.getName());
    }

    @Test
    void findById_notFound() {
        when(productViewRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.findById("2"));
    }

    @Test
    void findAll_success() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<ProductView> page = new PageImpl<>(List.of(productView), pageable, 1);
      when(productViewRepository.findAll(pageable)).thenReturn(page);
      Page<ProductView> result = productService.findAll(pageable);
      assertEquals(1, result.getTotalElements());
      assertEquals("Test", result.getContent().get(0).getName());
    }
}
