package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.domain.model.Franchise;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface CreateFranchiseUseCase {
    Mono<Franchise> create(@NotNull @Valid Franchise franchise);
}


