package com.red.franquicias.infrastructure.entrypoint.web.router;

import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ProductResponse;
import com.red.franquicias.infrastructure.entrypoint.web.dto.TopProductsResponse;
import com.red.franquicias.infrastructure.entrypoint.web.dto.UpdateProductNameRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.UpdateProductStockRequest;
import com.red.franquicias.infrastructure.entrypoint.web.handler.ProductHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ProductRouter {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/franchises/{franchiseId}/branches/{branchId}/products",
                    method = RequestMethod.POST,
                    beanClass = ProductHandler.class,
                    beanMethod = "create",
                    operation = @Operation(
                            operationId = "createProduct",
                            summary = "Create product",
                            description = "Creates a new product in a branch",
                            tags = {"Products"},
                            requestBody = @RequestBody(
                                    description = "Product data",
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = ProductRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Product created successfully",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Validation error"),
                                    @ApiResponse(responseCode = "404", description = "Franchise or branch not found"),
                                    @ApiResponse(responseCode = "409", description = "Duplicate product name in branch")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/franchises/{franchiseId}/branches/{branchId}/products/{productId}",
                    method = RequestMethod.PUT,
                    beanClass = ProductHandler.class,
                    beanMethod = "updateName",
                    operation = @Operation(
                            operationId = "updateProductName",
                            summary = "Update product name",
                            description = "Updates the name of an existing product",
                            tags = {"Products"},
                            requestBody = @RequestBody(
                                    description = "New product name",
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = UpdateProductNameRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Name updated successfully",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Validation error"),
                                    @ApiResponse(responseCode = "404", description = "Franchise, branch or product not found"),
                                    @ApiResponse(responseCode = "409", description = "Duplicate product name in branch")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock",
                    method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class,
                    beanMethod = "updateStock",
                    operation = @Operation(
                            operationId = "updateProductStock",
                            summary = "Update product stock",
                            description = "Updates the stock of an existing product",
                            tags = {"Products"},
                            requestBody = @RequestBody(
                                    description = "New product stock",
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = UpdateProductStockRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Stock updated successfully",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Validation error"),
                                    @ApiResponse(responseCode = "404", description = "Franchise, branch or product not found")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/franchises/{franchiseId}/branches/{branchId}/products/{productId}",
                    method = RequestMethod.DELETE,
                    beanClass = ProductHandler.class,
                    beanMethod = "remove",
                    operation = @Operation(
                            operationId = "removeProduct",
                            summary = "Delete product",
                            description = "Deletes a product from a branch",
                            tags = {"Products"},
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
                                    @ApiResponse(responseCode = "404", description = "Franchise, branch or product not found")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/franchises/{franchiseId}/branches/top-products",
                    method = RequestMethod.GET,
                    beanClass = ProductHandler.class,
                    beanMethod = "getTopProducts",
                    operation = @Operation(
                            operationId = "getTopProducts",
                            summary = "Get top products by branch",
                            description = "Gets the product with the highest stock for each branch of a franchise",
                            tags = {"Products"},
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Top products list retrieved successfully",
                                            content = @Content(schema = @Schema(implementation = TopProductsResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return RouterFunctions.route()
                .POST("/franchises/{franchiseId}/branches/{branchId}/products", handler::create)
                .PUT("/franchises/{franchiseId}/branches/{branchId}/products/{productId}", handler::updateName)
                .PATCH("/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock", handler::updateStock)
                .DELETE("/franchises/{franchiseId}/branches/{branchId}/products/{productId}", handler::remove)
                .GET("/franchises/{franchiseId}/branches/top-products", handler::getTopProducts)
                .build();
    }
}

