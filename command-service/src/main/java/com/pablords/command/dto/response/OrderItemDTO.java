package com.pablords.command.dto.response;

import java.util.List;
import java.util.UUID;

public record OrderItemDTO(
        UUID id,
        UUID productId,
        int quantity) {
}
