package com.pablords.query.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pablords.query.dto.ProductViewDTO;
import com.pablords.query.service.ProductService;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  public ResponseEntity<Page<ProductViewDTO>> findAll(Pageable pageable) {
    Page<ProductViewDTO> page = productService.findAll(pageable).map(ProductViewDTO::fromEntity);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductViewDTO> findById(@PathVariable String id) {
    var view = productService.findById(id);
    return ResponseEntity.ok(ProductViewDTO.fromEntity(view));
  }
}
