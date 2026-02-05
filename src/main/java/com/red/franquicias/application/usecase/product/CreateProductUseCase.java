package com.red.franquicias.application.usecase.product;

import com.red.franquicias.domain.model.Product;
import reactor.core.publisher.Mono;

public interface CreateProductUseCase {
    Mono<Product> create(Product product, Long franchiseId);
}

