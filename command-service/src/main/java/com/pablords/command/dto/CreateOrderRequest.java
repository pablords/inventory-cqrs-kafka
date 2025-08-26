package com.pablords.command.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
	@NotNull UUID productId,
	@Positive int quantity
) {}
