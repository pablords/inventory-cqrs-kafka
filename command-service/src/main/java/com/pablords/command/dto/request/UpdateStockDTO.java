package com.pablords.command.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateStockDTO(
    @Min(1) int amount
) {}
