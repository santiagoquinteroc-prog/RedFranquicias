package com.red.franquicias.infrastructure.entrypoint.web.mapper;

import com.red.franquicias.application.usecase.product.BranchTopProduct;
import com.red.franquicias.application.usecase.product.ProductInfo;
import com.red.franquicias.application.usecase.product.TopProductsResult;
import com.red.franquicias.domain.model.Product;
import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.ProductEntity;
import com.red.franquicias.infrastructure.entrypoint.web.dto.BranchTopProductResponse;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductInfoResponse;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductResponse;
import com.red.franquicias.infrastructure.entrypoint.web.dto.TopProductsResponse;

import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {
    public static Product toDomain(ProductRequest request, Long branchId) {
        return new Product(null, branchId, request.getName(), request.getStock());
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getBranchId(), product.getName(), product.getStock());
    }

    public static ProductEntity toEntity(Product product) {
        return new ProductEntity(product.getId(), product.getBranchId(), product.getName(), product.getStock());
    }

    public static Product toDomain(ProductEntity entity) {
        return new Product(entity.getId(), entity.getBranchId(), entity.getName(), entity.getStock());
    }

    public static TopProductsResponse toTopProductsResponse(TopProductsResult result) {
        List<BranchTopProductResponse> results = result.getResults().stream()
                .map(ProductMapper::toBranchTopProductResponse)
                .collect(Collectors.toList());
        return new TopProductsResponse(result.getFranchiseId(), result.getFranchiseName(), results);
    }

    private static BranchTopProductResponse toBranchTopProductResponse(BranchTopProduct branchTopProduct) {
        ProductInfo productInfo = branchTopProduct.getProduct();
        ProductInfoResponse productInfoResponse = new ProductInfoResponse(
                productInfo.getProductId(),
                productInfo.getName(),
                productInfo.getStock()
        );
        return new BranchTopProductResponse(
                branchTopProduct.getBranchId(),
                branchTopProduct.getBranchName(),
                productInfoResponse
        );
    }
}

