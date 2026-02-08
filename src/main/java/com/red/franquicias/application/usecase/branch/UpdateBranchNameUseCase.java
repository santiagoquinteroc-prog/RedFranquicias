package com.red.franquicias.application.usecase.branch;

import com.red.franquicias.domain.model.Branch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import reactor.core.publisher.Mono;

public interface UpdateBranchNameUseCase {
    Mono<Branch> updateName(
            @NotNull Long branchId,
            @NotNull Long franchiseId,
            @NotBlank(message = "Branch name is required")
            @Size(max = 60, message = "Branch name must not exceed 60 characters")
            String name
    );
}