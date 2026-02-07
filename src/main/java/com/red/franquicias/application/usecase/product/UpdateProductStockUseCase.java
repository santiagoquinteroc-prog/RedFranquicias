package com.red.franquicias.application.usecase.product;

import com.red.franquicias.domain.model.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface UpdateProductStockUseCase {
    Mono<Product> updateStock(
            @NotNull Long productId,
            @NotNull Long branchId,
            @NotNull Long franchiseId,
            @NotNull(message = "Product stock is required")
            @Min(value = 0, message = "Product stock must be greater than or equal to 0")
            Integer stock
    );
}