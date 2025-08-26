package com.pablords.command.dto;

import java.util.UUID;

public record CreateOrderRequest(UUID productId, int quantity) {}
