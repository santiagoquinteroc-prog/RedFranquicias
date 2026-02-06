package com.red.franquicias.infrastructure.entrypoint.web.dto;

public record BranchResponse(
        Long id,
        Long franchiseId,
        String name
) {
}
