package com.red.franquicias.infrastructure.entrypoint.web.router;

import com.red.franquicias.infrastructure.entrypoint.web.dto.FranchiseRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.FranchiseResponse;
import com.red.franquicias.infrastructure.entrypoint.web.handler.FranchiseHandler;
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
public class FranchiseRouter {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/franchises",
                    method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "create",
                    operation = @Operation(
                            operationId = "createFranchise",
                            summary = "Create franchise",
                            description = "Creates a new franchise",
                            tags = {"Franchises"},
                            requestBody = @RequestBody(
                                    description = "Franchise data",
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = FranchiseRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Franchise created successfully",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Validation error"),
                                    @ApiResponse(responseCode = "409", description = "Duplicate franchise name")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/franchises/{id}",
                    method = RequestMethod.PUT,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "updateName",
                    operation = @Operation(
                            operationId = "updateFranchiseName",
                            summary = "Update franchise name",
                            description = "Updates the name of an existing franchise",
                            tags = {"Franchises"},
                            requestBody = @RequestBody(
                                    description = "New franchise name",
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = FranchiseRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Name updated successfully",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Validation error"),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found"),
                                    @ApiResponse(responseCode = "409", description = "Duplicate franchise name")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return RouterFunctions.route()
                .POST("/franchises", handler::create)
                .PUT("/franchises/{id}", handler::updateName)
                .build();
    }
}


