package com.red.franquicias.infrastructure.entrypoint.web.handler;

import com.red.franquicias.application.usecase.product.CreateProductUseCase;
import com.red.franquicias.application.usecase.product.GetTopProductsByFranchiseUseCase;
import com.red.franquicias.application.usecase.product.RemoveProductUseCase;
import com.red.franquicias.application.usecase.product.UpdateProductNameUseCase;
import com.red.franquicias.application.usecase.product.UpdateProductStockUseCase;
import com.red.franquicias.domain.model.Product;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductResponse;
import com.red.franquicias.infrastructure.entrypoint.web.dto.TopProductsResponse;
import com.red.franquicias.infrastructure.entrypoint.web.dto.UpdateProductNameRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.UpdateProductStockRequest;
import com.red.franquicias.infrastructure.entrypoint.web.mapper.ProductMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Tag(name = "Products", description = "API for product management")
public class ProductHandler {
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductNameUseCase updateProductNameUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final RemoveProductUseCase removeProductUseCase;
    private final GetTopProductsByFranchiseUseCase getTopProductsByFranchiseUseCase;

    public ProductHandler(CreateProductUseCase createProductUseCase, UpdateProductNameUseCase updateProductNameUseCase, UpdateProductStockUseCase updateProductStockUseCase, RemoveProductUseCase removeProductUseCase, GetTopProductsByFranchiseUseCase getTopProductsByFranchiseUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.updateProductNameUseCase = updateProductNameUseCase;
        this.updateProductStockUseCase = updateProductStockUseCase;
        this.removeProductUseCase = removeProductUseCase;
        this.getTopProductsByFranchiseUseCase = getTopProductsByFranchiseUseCase;
    }

    @Operation(summary = "Create product", description = "Creates a new product in a branch")
    @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise or branch not found")
    @ApiResponse(responseCode = "409", description = "Duplicate product name in branch")
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

    @Operation(summary = "Update product name", description = "Updates the name of an existing product")
    @ApiResponse(responseCode = "200", description = "Name updated successfully", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise, branch or product not found")
    @ApiResponse(responseCode = "409", description = "Duplicate product name in branch")
    public Mono<ServerResponse> updateName(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        Long branchId = Long.parseLong(request.pathVariable("branchId"));
        Long productId = Long.parseLong(request.pathVariable("productId"));
        return request.bodyToMono(UpdateProductNameRequest.class)
                .flatMap(updateRequest -> updateProductNameUseCase.updateName(productId, branchId, franchiseId, updateRequest.name()))
                .flatMap(product -> {
                    var response = ProductMapper.toResponse(product);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }

    @Operation(summary = "Update product stock", description = "Updates the stock of an existing product")
    @ApiResponse(responseCode = "200", description = "Stock updated successfully", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise, branch or product not found")
    public Mono<ServerResponse> updateStock(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        Long branchId = Long.parseLong(request.pathVariable("branchId"));
        Long productId = Long.parseLong(request.pathVariable("productId"));
        return request.bodyToMono(UpdateProductStockRequest.class)
                .flatMap(updateRequest -> updateProductStockUseCase.updateStock(productId, branchId, franchiseId, updateRequest.stock()))
                .flatMap(product -> {
                    var response = ProductMapper.toResponse(product);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }

    @Operation(summary = "Delete product", description = "Deletes a product from a branch")
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    @ApiResponse(responseCode = "404", description = "Franchise, branch or product not found")
    public Mono<ServerResponse> remove(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        Long branchId = Long.parseLong(request.pathVariable("branchId"));
        Long productId = Long.parseLong(request.pathVariable("productId"));
        return removeProductUseCase.remove(productId, branchId, franchiseId)
                .then(ServerResponse.noContent().build());
    }

    @Operation(summary = "Get top products by branch", description = "Gets the product with the highest stock for each branch of a franchise")
    @ApiResponse(responseCode = "200", description = "Top products list retrieved successfully", content = @Content(schema = @Schema(implementation = TopProductsResponse.class)))
    @ApiResponse(responseCode = "404", description = "Franchise not found")
    public Mono<ServerResponse> getTopProducts(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        return getTopProductsByFranchiseUseCase.getTopProducts(franchiseId)
                .map(ProductMapper::toTopProductsResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }
}

