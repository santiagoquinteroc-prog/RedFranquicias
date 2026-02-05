package com.red.franquicias.application.usecase.product;

import reactor.core.publisher.Mono;

public interface RemoveProductUseCase {
    Mono<Void> remove(Long productId, Long branchId, Long franchiseId);
}

