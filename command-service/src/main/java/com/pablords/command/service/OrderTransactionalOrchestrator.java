package com.pablords.command.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablords.command.dto.request.CreateOrderDTO;
import com.pablords.command.dto.request.OrderItemCreateDTO;
import com.pablords.command.dto.response.OrderDTO;
import com.pablords.command.model.Order;
import com.pablords.command.model.OrderItem;
import com.pablords.command.model.Outbox;
import com.pablords.command.repository.IdempotencyRepository;
import com.pablords.command.repository.OutboxRepository;
import com.pablords.shared.events.StockReservationCancelledEvent;
import com.pablords.command.utils.Util;

@Service
@Slf4j
public class OrderTransactionalOrchestrator {

  private final ProductService productService;
  private final OrderService orderService;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final IdempotencyRepository idemRepo;

  public OrderTransactionalOrchestrator(ProductService productService,
      OrderService orderService,
      OutboxRepository outboxRepository,
      IdempotencyRepository idemRepo) {
    this.productService = productService;
    this.orderService = orderService;
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
      ObjectOptimisticLockingFailureException.class, // Hibernate/JPA otimista
      OptimisticLockException.class, // JPA otimista
      CannotAcquireLockException.class, // timeout de lock no DB
      DeadlockLoserDataAccessException.class, // deadlock detectado no DB
      PessimisticLockingFailureException.class // falha de lock pessimista (se usar em hotspots)
  }, maxAttempts = 3, backoff = @Backoff(delay = 300, maxDelay = 2000, multiplier = 2.0), exclude = {
      UnsupportedOperationException.class })
  @Transactional
  public Order processOrder(CreateOrderDTO request, String requestId) {
    try {
      String hash = Util.sha256(Util.canonicalJson(request));

      // 1) tenta “reservar” a chave
      int inserted = idemRepo.tryInsert(UUID.fromString(requestId), hash);
      if (inserted == 0) {
        // já existe → decide pela situação
        var existing = idemRepo.findById(UUID.fromString(requestId)).orElseThrow();
        if (!existing.getRequestHash().equals(hash)) {
          throw new ResponseStatusException(HttpStatus.CONFLICT,
              "Idempotency-Key já usada com payload diferente");
        }
        switch (existing.getStatus()) {
          case "SUCCEEDED" -> {
            return objectMapper.convertValue(existing.getResponseBody(), Order.class);
          }
          case "IN_PROGRESS" -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Pedido com mesma chave está em processamento. Tente novamente."
            // opcional: header Retry-After via ControllerAdvice
            );
          }
          case "FAILED" -> {
            // sua política: permitir novo processamento?
            // aqui eu devolveria 409 também
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tentativa anterior falhou.");
          }
        }
      }
    } catch (NoSuchAlgorithmException e) {
      log.error("Erro inesperado no processOrder, marcando idem como FAILED. requestId={}", requestId, e);
      throw new UncheckedIOException(new IOException(e));
    }

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

    Order order = orderService.createOrder(orderItems);

    // 3) persiste a resposta e finaliza a chave
    var ok = idemRepo.findById(UUID.fromString(requestId)).orElseThrow();
    ok.setStatus("SUCCEEDED");

    Map<String, Object> responseBody = objectMapper.convertValue(
        request,
        new TypeReference<Map<String, Object>>() {
        });
    ok.setResponseBody(responseBody);
    log.info("Order created {}", order.getId());
    return order;
  }

  /**
   * Compensação em transação própria: garante persistência do outbox
   * mesmo se a transação principal falhar.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  protected void saveCompensatingOutbox(UUID productId, int quantity, String reason, String details) {
    Map<String, Object> payload = objectMapper.convertValue(
        new StockReservationCancelledEvent(UUID.randomUUID().toString(), productId.toString(), quantity),
        new TypeReference<Map<String, Object>>() {
        });
    Outbox event = new Outbox();
    event.setId(UUID.randomUUID());
    event.setAggregateType("Stock");
    event.setAggregateId(productId.toString());
    event.setType("stock-reservation-cancelled");
    event.setPayload(payload);
    event.setCreatedAt(Instant.now());
    outboxRepository.save(event);
    log.info("Compensating outbox event created: {} (reason={}, details={})", event.getId(), reason, details);
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

  // ====== RECOVERs (um por exceção concorrencial) ======

  @Recover
  public Order recover(ObjectOptimisticLockingFailureException ex, UUID productId, int quantity) {
    log.error("Optimistic conflict after retries. Executing compensation... productId={}, qty={}", productId, quantity,
        ex);
    saveCompensatingOutbox(productId, quantity, "ObjectOptimisticLockingFailureException", ex.getMessage());
    throw new ConcurrencyConflictException(
        "Não foi possível processar o pedido por conflito de concorrência (optimistic).");
  }

  @Recover
  public Order recover(OptimisticLockException ex, UUID productId, int quantity) {
    log.error("OptimisticLockException after retries. Executing compensation... productId={}, qty={}", productId,
        quantity, ex);
    saveCompensatingOutbox(productId, quantity, "OptimisticLockException", ex.getMessage());
    throw new ConcurrencyConflictException(
        "Não foi possível processar o pedido por conflito de concorrência (optimistic).");
  }

  @Recover
  public Order recover(CannotAcquireLockException ex, UUID productId, int quantity) {
    log.error("CannotAcquireLock after retries. Executing compensation... productId={}, qty={}", productId, quantity,
        ex);
    saveCompensatingOutbox(productId, quantity, "CannotAcquireLockException", ex.getMessage());
    throw new ConcurrencyConflictException("Não foi possível processar o pedido (timeout de lock).");
  }

  @Recover
  public Order recover(DeadlockLoserDataAccessException ex, UUID productId, int quantity) {
    log.error("Deadlock after retries. Executing compensation... productId={}, qty={}", productId, quantity, ex);
    saveCompensatingOutbox(productId, quantity, "DeadlockLoserDataAccessException", ex.getMessage());
    throw new ConcurrencyConflictException("Não foi possível processar o pedido (deadlock).");
  }

  @Recover
  public Order recover(PessimisticLockingFailureException ex, UUID productId, int quantity) {
    log.error("Pessimistic lock failure after retries. Executing compensation... productId={}, qty={}", productId,
        quantity, ex);
    saveCompensatingOutbox(productId, quantity, "PessimisticLockingFailureException", ex.getMessage());
    throw new ConcurrencyConflictException("Não foi possível processar o pedido (pessimistic lock failure).");
  }

  /**
   * Exceção de domínio para o ControllerAdvice traduzir p/ HTTP 409 (Conflict) ou
   * 503 (Retry-After).
   */
  public static class ConcurrencyConflictException extends RuntimeException {
    public ConcurrencyConflictException(String message) {
      super(message);
    }
  }
}
