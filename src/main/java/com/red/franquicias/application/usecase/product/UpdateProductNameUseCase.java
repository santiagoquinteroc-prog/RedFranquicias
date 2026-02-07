package com.red.franquicias.application.usecase.product;

import com.red.franquicias.domain.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import reactor.core.publisher.Mono;

public interface UpdateProductNameUseCase {
    Mono<Product> updateName(
            @NotNull Long productId,
            @NotNull Long branchId,
            @NotNull Long franchiseId,
            @NotBlank(message = "Product name is required")
            @Size(max = 60, message = "Product name must not exceed 60 characters")
            String name
    );
}