package com.red.franquicias.infrastructure.entrypoint.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProductNameRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 60, message = "Product name must not exceed 60 characters")
        String name
) {
}
