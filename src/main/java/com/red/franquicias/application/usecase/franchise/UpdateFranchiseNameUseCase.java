package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface UpdateFranchiseNameUseCase {
    Mono<Franchise> updateName(Long id, String name);
}


