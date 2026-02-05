package com.red.franquicias.application.usecase.product;

import com.red.franquicias.domain.model.Product;
import reactor.core.publisher.Mono;

public interface UpdateProductStockUseCase {
    Mono<Product> updateStock(Long productId, Long branchId, Long franchiseId, Integer stock);
}

