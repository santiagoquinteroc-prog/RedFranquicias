package com.red.franquicias.infrastructure.drivenadapter.r2dbc.repository;

import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.ProductEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<ProductEntity, Long> {
    Mono<ProductEntity> findByIdAndBranchId(Long id, Long branchId);

    Mono<ProductEntity> findByNameAndBranchId(String name, Long branchId);
}

