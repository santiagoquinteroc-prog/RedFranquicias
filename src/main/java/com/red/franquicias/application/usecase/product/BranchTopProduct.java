package com.red.franquicias.application.usecase.product;

public record BranchTopProduct(
        Long branchId,
        String branchName,
        ProductInfo product
) {
}
