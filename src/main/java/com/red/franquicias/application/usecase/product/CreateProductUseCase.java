package com.red.franquicias.application.usecase.product;

import com.red.franquicias.domain.model.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface CreateProductUseCase {
    Mono<Product> create(@NotNull @Valid Product product, @NotNull Long franchiseId);
}