package com.pablords.command.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderDTO(
	@NotNull UUID productId,
	@Positive int quantity
) {}
