package com.red.franquicias.application.port.out;

import com.red.franquicias.domain.model.Branch;
import reactor.core.publisher.Mono;

public interface BranchRepositoryPort {
    Mono<Branch> save(Branch branch);

    Mono<Branch> findById(Long id);

    Mono<Branch> findByIdAndFranchiseId(Long id, Long franchiseId);

    Mono<Boolean> existsByNameAndFranchiseId(String name, Long franchiseId);
}

