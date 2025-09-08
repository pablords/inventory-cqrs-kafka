package com.pablords.command.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record OrderItemCreateDTO(
        @NotNull(message = "productId é obrigatório") UUID productId,

        @Positive(message = "quantity deve ser > 0") int quantity) {
}
