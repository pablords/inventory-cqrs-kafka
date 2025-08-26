package com.pablords.command.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pablords.command.model.Product;
import com.pablords.command.dto.ProductDTO;
import com.pablords.command.service.ProductService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products")
@Slf4j
public class ProductController {

  private final ProductService service;

  public ProductController(ProductService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<ProductDTO> create(@RequestParam String name, @RequestParam int initialQty) {
    log.info("Creating product with name: {} and initial quantity: {}", name, initialQty);
    Product product = service.createProduct(name, initialQty);
    return ResponseEntity.status(HttpStatus.CREATED).body(ProductDTO.fromEntity(product));
  }

  @PostMapping("/{id}/add")
  public ResponseEntity<ProductDTO> addStock(@PathVariable String id, @RequestParam int amount) {
    log.info("Added quantity: {}", amount);
    Product product = service.addStock(UUID.fromString(id), amount);
    return ResponseEntity.ok(ProductDTO.fromEntity(product));
  }

  @PostMapping("/{id}/remove")
  public ResponseEntity<ProductDTO> removeStock(@PathVariable String id, @RequestParam int amount) {
    log.info("Remove quantity: {}", amount);
    Product product = service.removeStock(UUID.fromString(id), amount);
    return ResponseEntity.ok(ProductDTO.fromEntity(product));
  }
}
