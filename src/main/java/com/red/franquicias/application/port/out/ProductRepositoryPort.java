package com.red.franquicias.application.port.out;

import com.red.franquicias.domain.model.Product;
import reactor.core.publisher.Mono;

public interface ProductRepositoryPort {
    Mono<Product> save(Product product);

    Mono<Product> findById(Long id);

    Mono<Product> findByIdAndBranchId(Long id, Long branchId);

    Mono<Boolean> existsByNameAndBranchId(String name, Long branchId);

    Mono<Void> deleteById(Long id);
}

