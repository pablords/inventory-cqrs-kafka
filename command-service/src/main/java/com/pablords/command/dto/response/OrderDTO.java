package com.pablords.command.dto.response;

import java.util.List;
import java.util.UUID;
import com.pablords.command.model.Order;

public record OrderDTO(
        UUID id,
        List<OrderItemDTO> items,
        String status) {
    public static OrderDTO fromEntity(Order order) {
        var itemsDto = order.getItems().stream()
                .map(i -> new OrderItemDTO(
                        i.getId(),
                        i.getProduct().getId(),
                        i.getQuantity()))
                .toList(); // aqui pode ser toList() pq é só para resposta

        return new OrderDTO(order.getId(), itemsDto, order.getStatus());
    }
}
