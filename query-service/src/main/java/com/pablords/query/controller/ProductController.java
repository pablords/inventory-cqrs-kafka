package com.pablords.query.controller;

import org.springframework.web.bind.annotation.*;

import com.pablords.query.model.ProductView;
import com.pablords.query.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  public List<ProductView> findAll() {
    return productService.findAll();
  }

  @GetMapping("/{id}")
  public ProductView findById(@PathVariable String id) {
    return productService.findById(id);
  }
}
