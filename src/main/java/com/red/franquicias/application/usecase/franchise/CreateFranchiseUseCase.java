package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface CreateFranchiseUseCase {
    Mono<Franchise> create(Franchise franchise);
}


