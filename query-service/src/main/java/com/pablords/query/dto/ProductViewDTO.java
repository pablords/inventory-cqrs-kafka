package com.pablords.query.dto;

public record ProductViewDTO(String id, String name, int quantity, String topic, String log) {
    public static ProductViewDTO fromEntity(com.pablords.query.model.ProductView view) {
        return new ProductViewDTO(
            view.getId(),
            view.getName(),
            view.getQuantity(),
            view.getTopic(),
            view.getLog()
        );
    }
}
