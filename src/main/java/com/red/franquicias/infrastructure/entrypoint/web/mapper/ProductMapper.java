package com.red.franquicias.infrastructure.entrypoint.web.mapper;

import com.red.franquicias.domain.model.Product;
import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.ProductEntity;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductResponse;

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
}

