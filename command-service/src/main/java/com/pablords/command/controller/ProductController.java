package com.pablords.command.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pablords.command.model.Product;
import com.pablords.command.dto.request.CreateProductDTO;
import com.pablords.command.dto.request.UpdateStockDTO;
import com.pablords.command.dto.response.ProductDTO;

import jakarta.validation.Valid;
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
  public ResponseEntity<ProductDTO> create(@Valid @RequestBody CreateProductDTO request) {
    log.info("Creating product with name: {} and initial quantity: {}", request.name(), request.initialQty());
    Product product = service.createProduct(request.name(), request.initialQty());
    return ResponseEntity.status(HttpStatus.CREATED).body(ProductDTO.fromEntity(product));
  }

  @PostMapping("/{id}/add")
  public ResponseEntity<ProductDTO> addStock(@PathVariable String id, @Valid @RequestBody UpdateStockDTO request) {
    log.info("Added quantity: {}", request.amount());
    Product product = service.addStock(UUID.fromString(id), request.amount());
    return ResponseEntity.ok(ProductDTO.fromEntity(product));
  }

  @PostMapping("/{id}/remove")
  public ResponseEntity<ProductDTO> removeStock(@PathVariable String id, @Valid @RequestBody UpdateStockDTO request) {
    log.info("Remove quantity: {}", request.amount());
    Product product = service.removeStock(UUID.fromString(id), request.amount());
    return ResponseEntity.ok(ProductDTO.fromEntity(product));
  }
}
