package com.red.franquicias.infrastructure.entrypoint.web.dto;

import java.util.List;

public record TopProductsResponse(
        Long franchiseId,
        String franchiseName,
        List<BranchTopProductResponse> results
) {
}
