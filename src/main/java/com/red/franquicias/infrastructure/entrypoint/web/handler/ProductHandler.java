package com.red.franquicias.infrastructure.entrypoint.web.handler;

import com.red.franquicias.application.usecase.product.CreateProductUseCase;
import com.red.franquicias.application.usecase.product.UpdateProductNameUseCase;
import com.red.franquicias.application.usecase.product.UpdateProductStockUseCase;
import com.red.franquicias.domain.model.Product;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.UpdateProductNameRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.UpdateProductStockRequest;
import com.red.franquicias.infrastructure.entrypoint.web.mapper.ProductMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductNameUseCase updateProductNameUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;

    public ProductHandler(CreateProductUseCase createProductUseCase, UpdateProductNameUseCase updateProductNameUseCase, UpdateProductStockUseCase updateProductStockUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.updateProductNameUseCase = updateProductNameUseCase;
        this.updateProductStockUseCase = updateProductStockUseCase;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        Long branchId = Long.parseLong(request.pathVariable("branchId"));
        return request.bodyToMono(ProductRequest.class)
                .flatMap(productRequest -> {
                    Product product = ProductMapper.toDomain(productRequest, branchId);
                    return createProductUseCase.create(product, franchiseId);
                })
                .flatMap(product -> {
                    var response = ProductMapper.toResponse(product);
                    return ServerResponse.status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }

    public Mono<ServerResponse> updateName(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        Long branchId = Long.parseLong(request.pathVariable("branchId"));
        Long productId = Long.parseLong(request.pathVariable("productId"));
        return request.bodyToMono(UpdateProductNameRequest.class)
                .flatMap(updateRequest -> updateProductNameUseCase.updateName(productId, branchId, franchiseId, updateRequest.getName()))
                .flatMap(product -> {
                    var response = ProductMapper.toResponse(product);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        Long branchId = Long.parseLong(request.pathVariable("branchId"));
        Long productId = Long.parseLong(request.pathVariable("productId"));
        return request.bodyToMono(UpdateProductStockRequest.class)
                .flatMap(updateRequest -> updateProductStockUseCase.updateStock(productId, branchId, franchiseId, updateRequest.getStock()))
                .flatMap(product -> {
                    var response = ProductMapper.toResponse(product);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }
}

