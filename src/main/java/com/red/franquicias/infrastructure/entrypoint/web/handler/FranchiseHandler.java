package com.red.franquicias.infrastructure.entrypoint.web.handler;

import com.red.franquicias.application.usecase.franchise.CreateFranchiseUseCase;
import com.red.franquicias.application.usecase.franchise.UpdateFranchiseNameUseCase;
import com.red.franquicias.domain.model.Franchise;
import com.red.franquicias.infrastructure.entrypoint.web.dto.FranchiseRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.FranchiseResponse;
import com.red.franquicias.infrastructure.entrypoint.web.mapper.FranchiseMapper;
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
@Tag(name = "Franchises", description = "API for franchise management")
public class FranchiseHandler {
    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final UpdateFranchiseNameUseCase updateFranchiseNameUseCase;

    public FranchiseHandler(CreateFranchiseUseCase createFranchiseUseCase, UpdateFranchiseNameUseCase updateFranchiseNameUseCase) {
        this.createFranchiseUseCase = createFranchiseUseCase;
        this.updateFranchiseNameUseCase = updateFranchiseNameUseCase;
    }

    @Operation(summary = "Create franchise", description = "Creates a new franchise")
    @ApiResponse(responseCode = "201", description = "Franchise created successfully", content = @Content(schema = @Schema(implementation = FranchiseResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "409", description = "Duplicate franchise name")
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(FranchiseRequest.class)
                .flatMap(franchiseRequest -> {
                    Franchise franchise = FranchiseMapper.toDomain(franchiseRequest);
                    return createFranchiseUseCase.create(franchise);
                })
                .flatMap(franchise -> {
                    var response = FranchiseMapper.toResponse(franchise);
                    return ServerResponse.status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }

    @Operation(summary = "Update franchise name", description = "Updates the name of an existing franchise")
    @ApiResponse(responseCode = "200", description = "Name updated successfully", content = @Content(schema = @Schema(implementation = FranchiseResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise not found")
    @ApiResponse(responseCode = "409", description = "Duplicate franchise name")
    public Mono<ServerResponse> updateName(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(FranchiseRequest.class)
                .flatMap(franchiseRequest -> updateFranchiseNameUseCase.updateName(id, franchiseRequest.name()))
                .flatMap(franchise -> {
                    var response = FranchiseMapper.toResponse(franchise);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }
}


