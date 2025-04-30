package com.pablords.command.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.pablords.command.domain.Product;
import com.pablords.command.service.ProductCommandService;

@RestController
@RequestMapping("/products")
public class ProductCommandController {

  private final ProductCommandService service;
  private static final Logger log = LoggerFactory.getLogger(ProductCommandController.class);

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
