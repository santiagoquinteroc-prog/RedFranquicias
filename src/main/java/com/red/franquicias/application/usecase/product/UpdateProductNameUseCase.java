package com.red.franquicias.application.usecase.product;

import com.red.franquicias.domain.model.Product;
import reactor.core.publisher.Mono;

public interface UpdateProductNameUseCase {
    Mono<Product> updateName(Long productId, Long branchId, Long franchiseId, String name);
}

