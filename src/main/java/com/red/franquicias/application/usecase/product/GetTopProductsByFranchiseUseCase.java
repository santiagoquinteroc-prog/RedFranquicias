package com.red.franquicias.application.usecase.product;

import com.red.franquicias.domain.model.Product;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GetTopProductsByFranchiseUseCase {
    Mono<TopProductsResult> getTopProducts(Long franchiseId);
}

