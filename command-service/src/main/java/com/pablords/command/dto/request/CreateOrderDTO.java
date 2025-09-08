package com.pablords.command.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderDTO(
    @NotEmpty(message = "A lista de itens não pode ser vazia")
    List<@Valid OrderItemCreateDTO> items,
    String idempotencyKey
) {
}
