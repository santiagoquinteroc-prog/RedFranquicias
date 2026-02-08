package com.red.franquicias.application.usecase.product;

public record ProductInfo(
        Long productId,
        String name,
        Integer stock
) {
}
