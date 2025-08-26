package com.pablords.command.dto;

import java.util.UUID;
import com.pablords.command.model.Order;

public record OrderDTO(
    UUID id,
    UUID productId,
    int quantity,
    String status
) {
    public static OrderDTO fromEntity(Order order) {
        return new OrderDTO(
            order.getId(),
            order.getProductId(),
            order.getQuantity(),
            order.getStatus()
        );
    }
}
