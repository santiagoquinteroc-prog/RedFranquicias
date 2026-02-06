package com.red.franquicias.application.port.out;

import com.red.franquicias.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface FranchiseRepositoryPort {
    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> findById(Long id);

    Mono<Boolean> existsByName(String name);
}


