package com.red.franquicias.infrastructure.entrypoint.web.dto;

public record ProductResponse(
        Long id,
        Long branchId,
        String name,
        Integer stock
) {
}
