package com.red.franquicias.infrastructure.entrypoint.web.handler;

import com.red.franquicias.application.usecase.product.CreateProductUseCase;
import com.red.franquicias.application.usecase.product.GetTopProductsByFranchiseUseCase;
import com.red.franquicias.application.usecase.product.RemoveProductUseCase;
import com.red.franquicias.application.usecase.product.UpdateProductNameUseCase;
import com.red.franquicias.application.usecase.product.UpdateProductStockUseCase;
import com.red.franquicias.domain.exception.BusinessException;
import com.red.franquicias.domain.exception.TechnicalException;
import com.red.franquicias.domain.model.Product;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductResponse;
import com.red.franquicias.infrastructure.entrypoint.web.dto.TopProductsResponse;
import com.red.franquicias.infrastructure.entrypoint.web.dto.UpdateProductNameRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.UpdateProductStockRequest;
import com.red.franquicias.infrastructure.entrypoint.web.mapper.ProductMapper;
import com.red.franquicias.infrastructure.entrypoint.web.util.WebHandlerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "API for product management")
public class ProductHandler {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductNameUseCase updateProductNameUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final RemoveProductUseCase removeProductUseCase;
    private final GetTopProductsByFranchiseUseCase getTopProductsByFranchiseUseCase;
    private final Validator validator;

    @Operation(summary = "Create product", description = "Creates a new product in a branch")
    @ApiResponse(responseCode = "201", description = "Product created successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise or branch not found")
    @ApiResponse(responseCode = "409", description = "Duplicate product name in branch")
    public Mono<ServerResponse> create(ServerRequest request) {
        return Mono.zip(
                        WebHandlerUtils.parseLongPathVariable(request, "franchiseId"),
                        WebHandlerUtils.parseLongPathVariable(request, "branchId")
                )
                .flatMap(tuple ->
                        request.bodyToMono(ProductRequest.class)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Body is required")))
                                .flatMap(productRequest -> {
                                    WebHandlerUtils.validate(validator, productRequest);
                                    Product product = ProductMapper.toDomain(productRequest, tuple.getT2());
                                    return createProductUseCase.create(product, tuple.getT1());
                                })
                                .doOnSuccess(product -> log.info("Product created successfully: {}", product.getId()))
                )
                .flatMap(product ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ProductMapper.toResponse(product))
                )
                .doOnError(ex -> log.error("Error creating product", ex))
                .onErrorResume(BusinessException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                WebHandlerUtils.mapBusinessStatus(ex.getTechnicalMessage()),
                                ex.getTechnicalMessage()
                        )
                )
                .onErrorResume(TechnicalException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ex.getTechnicalMessage()
                        )
                )
                .onErrorResume(IllegalArgumentException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage()
                        )
                )
                .onErrorResume(ex -> {
                    log.error("Unexpected error creating product", ex);
                    return WebHandlerUtils.error(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                });
    }

    @Operation(summary = "Update product name", description = "Updates the name of an existing product")
    @ApiResponse(responseCode = "200", description = "Name updated successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise, branch or product not found")
    @ApiResponse(responseCode = "409", description = "Duplicate product name in branch")
    public Mono<ServerResponse> updateName(ServerRequest request) {
        return Mono.zip(
                        WebHandlerUtils.parseLongPathVariable(request, "franchiseId"),
                        WebHandlerUtils.parseLongPathVariable(request, "branchId"),
                        WebHandlerUtils.parseLongPathVariable(request, "productId")
                )
                .flatMap(tuple ->
                        request.bodyToMono(UpdateProductNameRequest.class)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Body is required")))
                                .flatMap(updateRequest -> {
                                    WebHandlerUtils.validate(validator, updateRequest);
                                    return updateProductNameUseCase.updateName(
                                            tuple.getT3(),
                                            tuple.getT2(),
                                            tuple.getT1(),
                                            updateRequest.name()
                                    );
                                })
                                .doOnSuccess(product -> log.info("Product name updated successfully: {}", product.getId()))
                )
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ProductMapper.toResponse(product))
                )
                .doOnError(ex -> log.error("Error updating product name", ex))
                .onErrorResume(BusinessException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                WebHandlerUtils.mapBusinessStatus(ex.getTechnicalMessage()),
                                ex.getTechnicalMessage()
                        )
                )
                .onErrorResume(TechnicalException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ex.getTechnicalMessage()
                        )
                )
                .onErrorResume(IllegalArgumentException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage()
                        )
                )
                .onErrorResume(ex -> {
                    log.error("Unexpected error updating product name", ex);
                    return WebHandlerUtils.error(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                });
    }

    @Operation(summary = "Update product stock", description = "Updates the stock of an existing product")
    @ApiResponse(responseCode = "200", description = "Stock updated successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise, branch or product not found")
    public Mono<ServerResponse> updateStock(ServerRequest request) {
        return Mono.zip(
                        WebHandlerUtils.parseLongPathVariable(request, "franchiseId"),
                        WebHandlerUtils.parseLongPathVariable(request, "branchId"),
                        WebHandlerUtils.parseLongPathVariable(request, "productId")
                )
                .flatMap(tuple ->
                        request.bodyToMono(UpdateProductStockRequest.class)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Body is required")))
                                .flatMap(updateRequest -> {
                                    WebHandlerUtils.validate(validator, updateRequest);
                                    return updateProductStockUseCase.updateStock(
                                            tuple.getT3(),
                                            tuple.getT2(),
                                            tuple.getT1(),
                                            updateRequest.stock()
                                    );
                                })
                                .doOnSuccess(product -> log.info("Product stock updated successfully: {}", product.getId()))
                )
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ProductMapper.toResponse(product))
                )
                .doOnError(ex -> log.error("Error updating product stock", ex))
                .onErrorResume(BusinessException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                WebHandlerUtils.mapBusinessStatus(ex.getTechnicalMessage()),
                                ex.getTechnicalMessage()
                        )
                )
                .onErrorResume(TechnicalException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ex.getTechnicalMessage()
                        )
                )
                .onErrorResume(IllegalArgumentException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage()
                        )
                )
                .onErrorResume(ex -> {
                    log.error("Unexpected error updating product stock", ex);
                    return WebHandlerUtils.error(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                });
    }

    @Operation(summary = "Delete product", description = "Deletes a product from a branch")
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    @ApiResponse(responseCode = "404", description = "Franchise, branch or product not found")
    public Mono<ServerResponse> remove(ServerRequest request) {
        return Mono.zip(
                        WebHandlerUtils.parseLongPathVariable(request, "franchiseId"),
                        WebHandlerUtils.parseLongPathVariable(request, "branchId"),
                        WebHandlerUtils.parseLongPathVariable(request, "productId")
                )
                .flatMap(tuple ->
                        removeProductUseCase.remove(tuple.getT3(), tuple.getT2(), tuple.getT1())
                                .doOnSuccess(v -> log.info("Product removed successfully: {}", tuple.getT3()))
                )
                .then(ServerResponse.noContent().build())
                .doOnError(ex -> log.error("Error removing product", ex))
                .onErrorResume(BusinessException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                WebHandlerUtils.mapBusinessStatus(ex.getTechnicalMessage()),
                                ex.getTechnicalMessage()
                        )
                )
                .onErrorResume(TechnicalException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ex.getTechnicalMessage()
                        )
                )
                .onErrorResume(IllegalArgumentException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage()
                        )
                )
                .onErrorResume(ex -> {
                    log.error("Unexpected error removing product", ex);
                    return WebHandlerUtils.error(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                });
    }

    @Operation(summary = "Get top products by branch", description = "Gets the product with the highest stock for each branch of a franchise")
    @ApiResponse(responseCode = "200", description = "Top products list retrieved successfully",
            content = @Content(schema = @Schema(implementation = TopProductsResponse.class)))
    @ApiResponse(responseCode = "404", description = "Franchise not found")
    public Mono<ServerResponse> getTopProducts(ServerRequest request) {
        return WebHandlerUtils.parseLongPathVariable(request, "franchiseId")
                .flatMap(franchiseId -> getTopProductsByFranchiseUseCase.getTopProducts(franchiseId))
                .map(ProductMapper::toTopProductsResponse)
                .flatMap(response ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response)
                )
                .doOnError(ex -> log.error("Error retrieving top products", ex))
                .onErrorResume(BusinessException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                WebHandlerUtils.mapBusinessStatus(ex.getTechnicalMessage()),
                                ex.getTechnicalMessage()
                        )
                )
                .onErrorResume(TechnicalException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ex.getTechnicalMessage()
                        )
                )
                .onErrorResume(IllegalArgumentException.class, ex ->
                        WebHandlerUtils.error(
                                request,
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage()
                        )
                )
                .onErrorResume(ex -> {
                    log.error("Unexpected error retrieving top products", ex);
                    return WebHandlerUtils.error(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                });
    }
}
