package com.red.franquicias.infrastructure.entrypoint.web.router;

import com.red.franquicias.infrastructure.entrypoint.web.dto.BranchRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.BranchResponse;
import com.red.franquicias.infrastructure.entrypoint.web.handler.BranchHandler;
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
public class BranchRouter {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/franchises/{franchiseId}/branches",
                    method = RequestMethod.POST,
                    beanClass = BranchHandler.class,
                    beanMethod = "create",
                    operation = @Operation(
                            operationId = "createBranch",
                            summary = "Create branch",
                            description = "Creates a new branch for a franchise",
                            tags = {"Branches"},
                            requestBody = @RequestBody(
                                    description = "Branch data",
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = BranchRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Branch created successfully",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Validation error"),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found"),
                                    @ApiResponse(responseCode = "409", description = "Duplicate branch name")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/franchises/{franchiseId}/branches/{branchId}",
                    method = RequestMethod.PUT,
                    beanClass = BranchHandler.class,
                    beanMethod = "updateName",
                    operation = @Operation(
                            operationId = "updateBranchName",
                            summary = "Update branch name",
                            description = "Updates the name of an existing branch",
                            tags = {"Branches"},
                            requestBody = @RequestBody(
                                    description = "New branch name",
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = BranchRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Name updated successfully",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Validation error"),
                                    @ApiResponse(responseCode = "404", description = "Franchise or branch not found"),
                                    @ApiResponse(responseCode = "409", description = "Duplicate branch name")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> branchRoutes(BranchHandler handler) {
        return RouterFunctions.route()
                .POST("/franchises/{franchiseId}/branches", handler::create)
                .PUT("/franchises/{franchiseId}/branches/{branchId}", handler::updateName)
                .build();
    }
}

