package com.pablords.command.dto;

import java.util.UUID;

public record OrderDTO(UUID id, UUID productId, int quantity, String status) {
    public static OrderDTO fromEntity(com.pablords.command.model.Order order) {
        return new OrderDTO(
            order.getId(),
            order.getProductId(),
            order.getQuantity(),
            order.getStatus()
        );
    }
}
