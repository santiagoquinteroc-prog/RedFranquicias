package com.red.franquicias.infrastructure.entrypoint.web.dto;

public record ProductInfoResponse(
        Long productId,
        String name,
        Integer stock
) {
}
