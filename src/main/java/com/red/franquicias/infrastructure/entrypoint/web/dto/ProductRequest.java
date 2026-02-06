package com.red.franquicias.infrastructure.entrypoint.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 60, message = "Product name must not exceed 60 characters")
        String name,
        @NotNull(message = "Product stock is required")
        @Min(value = 0, message = "Product stock must be greater than or equal to 0")
        Integer stock
) {
}
