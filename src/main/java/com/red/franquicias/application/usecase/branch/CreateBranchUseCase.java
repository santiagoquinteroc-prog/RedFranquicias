package com.red.franquicias.application.usecase.branch;

import com.red.franquicias.domain.model.Branch;
import reactor.core.publisher.Mono;

public interface CreateBranchUseCase {
    Mono<Branch> create(Branch branch);
}

