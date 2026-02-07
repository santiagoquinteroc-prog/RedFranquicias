package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.domain.model.Franchise;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import reactor.core.publisher.Mono;

public interface UpdateFranchiseNameUseCase {
    Mono<Franchise> updateName(
            @NotNull Long id,
            @NotBlank(message = "Franchise name is required")
            @Size(max = 60, message = "Franchise name must not exceed 60 characters")
            String name
    );
}