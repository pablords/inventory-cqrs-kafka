package com.pablords.command.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.pablords.command.domain.Product;
import com.pablords.command.service.ProductCommandService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products")
@Slf4j
public class ProductCommandController {

  private final ProductCommandService service;

  public ProductCommandController(ProductCommandService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Product create(@RequestParam String name, @RequestParam int initialQty) {
    log.info("Creating product with name: {} and initial quantity: {}", name, initialQty);
    return service.createProduct(name, initialQty);
  }

  @PostMapping("/{id}/add")
  public Product addStock(@PathVariable String id, @RequestParam int amount) {
    log.info("Added quantity: {}", amount);
    return service.addStock(UUID.fromString(id), amount);
  }

  @PostMapping("/{id}/remove")
  public Product removeStock(@PathVariable String id, @RequestParam int amount) {
    log.info("Remove quantity: {}", amount);
    return service.removeStock(UUID.fromString(id), amount);
  }
}
