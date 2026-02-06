package com.red.franquicias.infrastructure.drivenadapter.r2dbc.repository;

import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.FranchiseEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface FranchiseRepository extends ReactiveCrudRepository<FranchiseEntity, Long> {
    Mono<FranchiseEntity> findByName(String name);
}


