package com.red.franquicias.application.usecase.branch;

import com.red.franquicias.domain.model.Branch;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface CreateBranchUseCase {
    Mono<Branch> create(@NotNull @Valid Branch branch);
}

