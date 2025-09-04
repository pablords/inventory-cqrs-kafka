package com.pablords.command.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateProductDTO(
    @NotBlank String name,
    @Min(0) int initialQty
) {}
