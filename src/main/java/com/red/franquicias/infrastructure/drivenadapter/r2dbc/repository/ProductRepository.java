package com.red.franquicias.infrastructure.drivenadapter.r2dbc.repository;

import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.ProductEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<ProductEntity, Long> {
    Mono<ProductEntity> findByIdAndBranchId(Long id, Long branchId);

    Mono<ProductEntity> findByNameAndBranchId(String name, Long branchId);

    @Query("SELECT * FROM products WHERE branch_id = :branchId ORDER BY stock DESC LIMIT 1")
    Mono<ProductEntity> findTopByBranchIdOrderByStockDesc(Long branchId);
}

