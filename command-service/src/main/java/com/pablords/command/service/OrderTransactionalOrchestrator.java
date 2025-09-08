package com.pablords.command.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablords.command.dto.request.CreateOrderDTO;
import com.pablords.command.dto.response.OrderDTO;
import com.pablords.command.dto.response.OrderItemDTO;
import com.pablords.command.exception.ConcurrencyConflictException;
import com.pablords.command.model.Order;
import com.pablords.command.model.OrderItem;
import com.pablords.command.repository.IdempotencyRepository;
import com.pablords.command.repository.OrderRepository;
import com.pablords.command.repository.OutboxRepository;

import com.pablords.command.utils.Util;

@Service
@Slf4j
public class OrderTransactionalOrchestrator {

  private final ProductService productService;
  private final OrderRepository orderRepository;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final IdempotencyRepository idemRepo;

  public OrderTransactionalOrchestrator(ProductService productService,
      OrderRepository orderRepository,
      OutboxRepository outboxRepository,
      IdempotencyRepository idemRepo) {
    this.productService = productService;
    this.orderRepository = orderRepository;
    this.outboxRepository = outboxRepository;
    this.idemRepo = idemRepo;
  }

  /**
   * Observações:
   * - Cada tentativa roda em uma transação separada (rollback → reexecuta).
   * - Evite RuntimeException genérica no Retryable para não "repetir" erros
   * não-transientes.
   * - Ajuste delays conforme métricas reais de conflito.
   */
  @Retryable(value = {
      CannotAcquireLockException.class,
      PessimisticLockingFailureException.class,
  }, maxAttempts = 3, backoff = @Backoff(delay = 300, maxDelay = 2000, multiplier = 2.0), exclude = {
      UnsupportedOperationException.class })
  @Transactional
  public OrderDTO processOrder(CreateOrderDTO request, String requestId) {
    var idemHit = this.handleIdempotencyData(request, requestId);
    if (idemHit.isPresent())
      return idemHit.get();

    request.items().stream().forEach(item -> productService.removeStock(item.productId(), item.quantity()));
    var orderItems = request.items().stream()
        .map(dto -> {
          var item = new OrderItem();
          var product = productService.findById(dto.productId());
          item.setProduct(product);
          item.setQuantity(dto.quantity());
          log.info("Order item prepared: productId={}, qty={}", product.getId(), dto.quantity());
          return item;
        })
        .collect(Collectors.toList());

    Order order = new Order();
    order.setStatus("COMPLETED");
    order.setItems(orderItems);
    orderRepository.save(order);

    // 3) persiste a resposta e finaliza a chave
    var ok = idemRepo.findById(UUID.fromString(requestId)).orElseThrow();
    ok.setStatus("SUCCEEDED");

    var responseItems = order.getItems().stream()
        .map(i -> new OrderItemDTO(
            i.getId(),
            i.getProduct().getId(),
            i.getQuantity()))
        .toList();

    Map<String, Object> responseBody = objectMapper.convertValue(
        new OrderDTO(order.getId(), responseItems, order.getStatus()),
        new TypeReference<Map<String, Object>>() {
        });
    ok.setResponseBody(responseBody);
    idemRepo.save(ok);
    log.info("Order created {}", order.getId());

    var dto = new OrderDTO(order.getId(), responseItems, order.getStatus());
    return dto;
  }


  @Transactional(noRollbackFor = ResponseStatusException.class)
  public void markFailedIdem(UUID idemKey, String hash, String reason) {
    idemRepo.findById(idemKey).ifPresent(k -> {
      if (k.getRequestHash().equals(hash)) {
        k.setStatus("FAILED");
        idemRepo.save(k);
      }
    });
  }

  private Optional<OrderDTO> handleIdempotencyData(CreateOrderDTO request, String requestId) {
    try {
      String hash = Util.sha256(Util.canonicalJson(request));
      // 1) tenta “reservar” a chave
      int inserted = idemRepo.tryInsert(UUID.fromString(requestId), hash);
      if (inserted == 0) {
        // já existe → decide pela situação
        var existing = idemRepo.findById(UUID.fromString(requestId)).orElseThrow();
        if (!existing.getRequestHash().equals(hash)) {
          log.warn("Idem key reuse with different payload! requestId={}", requestId);
          throw new ResponseStatusException(HttpStatus.CONFLICT,
              "Idempotency-Key já usada com payload diferente");
        }
        switch (existing.getStatus()) {
          case "SUCCEEDED" -> {
            // idem perfeito: devolve o resultado
            log.info("Idem key hit, returning previous result. requestId={}", requestId);
            return Optional.of(objectMapper.convertValue(existing.getResponseBody(), OrderDTO.class));
          }
          case "IN_PROGRESS" -> {
            // sua política: esperar? rejeitar?
            // aqui eu rejeitaria com 409 Conflict
            log.info("Idem key in progress, rejecting new request. requestId={}", requestId);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Pedido com mesma chave está em processamento. Tente novamente."
            // opcional: header Retry-After via ControllerAdvice
            );
          }
          case "FAILED" -> {
            // sua política: permitir novo processamento?
            // aqui eu devolveria 409 também
            log.info("Idem key marked as FAILED, rejecting new request. requestId={}", requestId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tentativa anterior falhou.");
          }
        }
      }
    } catch (NoSuchAlgorithmException e) {
      log.error("Unexpected error in process order, idem checked with FAILED. requestId={}", requestId, e);
      throw new UncheckedIOException(new IOException(e));
    }
    return Optional.empty();
  }

  // ====== RECOVERs (um por exceção concorrencial) ======

  @Recover
  public OrderDTO recover(CannotAcquireLockException ex, CreateOrderDTO req, String requestId) {
    log.error("Order recover CannotAcquireLock reqId={}", requestId, ex);
    markFailedIdem(UUID.fromString(requestId), "CannotAcquireLock", ex.getMessage());
    throw new ConcurrencyConflictException("Timeout de lock, tente novamente.");
  }

  @Recover
  public OrderDTO recover(PessimisticLockingFailureException ex, CreateOrderDTO req, String requestId) {
    log.error("Order recover PessimisticLock reqId={}", requestId, ex);
    markFailedIdem(UUID.fromString(requestId), "PessimisticLock", ex.getMessage());
    throw new ConcurrencyConflictException("Timeout de lock, tente novamente.");
  }

}
