package com.pablords.command.dto;
import com.pablords.command.model.Product;

import java.util.UUID;

public record ProductDTO(UUID id, String name, Integer quantity, Long version) {
    public static ProductDTO fromEntity(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getQuantity(),
            product.getVersion()
        );
    }
}
