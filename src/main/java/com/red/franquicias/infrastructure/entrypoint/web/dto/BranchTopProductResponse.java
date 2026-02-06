package com.red.franquicias.infrastructure.entrypoint.web.dto;

public record BranchTopProductResponse(
        Long branchId,
        String branchName,
        ProductInfoResponse product
) {
}
