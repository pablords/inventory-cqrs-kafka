package com.pablords.query.controller;


import org.springframework.web.bind.annotation.*;

import com.pablords.query.model.ProductView;
import com.pablords.query.repository.ProductViewRepository;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductQueryController {

    private final ProductViewRepository repository;

    public ProductQueryController(ProductViewRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ProductView> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ProductView findById(@PathVariable String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found in read model"));
    }
}
