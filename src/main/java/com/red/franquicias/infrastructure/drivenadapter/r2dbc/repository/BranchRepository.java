package com.red.franquicias.infrastructure.drivenadapter.r2dbc.repository;

import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.BranchEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BranchRepository extends ReactiveCrudRepository<BranchEntity, Long> {
    Mono<BranchEntity> findByIdAndFranchiseId(Long id, Long franchiseId);

    Mono<BranchEntity> findByNameAndFranchiseId(String name, Long franchiseId);

    Flux<BranchEntity> findByFranchiseId(Long franchiseId);
}

