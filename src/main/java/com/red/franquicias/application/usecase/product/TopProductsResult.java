package com.red.franquicias.application.usecase.product;

import java.util.List;

public record TopProductsResult(
        Long franchiseId,
        String franchiseName,
        List<BranchTopProduct> results
) {
}
