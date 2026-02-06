package com.red.franquicias.infrastructure.entrypoint.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStockRequest(
        @NotNull(message = "Product stock is required")
        @Min(value = 0, message = "Product stock must be greater than or equal to 0")
        Integer stock
) {
}
