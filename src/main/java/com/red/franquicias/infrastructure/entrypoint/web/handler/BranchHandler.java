package com.red.franquicias.infrastructure.entrypoint.web.handler;

import com.red.franquicias.application.usecase.branch.CreateBranchUseCase;
import com.red.franquicias.application.usecase.branch.UpdateBranchNameUseCase;
import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.infrastructure.entrypoint.web.dto.BranchRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.BranchResponse;
import com.red.franquicias.infrastructure.entrypoint.web.mapper.BranchMapper;
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
@Tag(name = "Branches", description = "API for branch management")
public class BranchHandler {
    private final CreateBranchUseCase createBranchUseCase;
    private final UpdateBranchNameUseCase updateBranchNameUseCase;

    public BranchHandler(CreateBranchUseCase createBranchUseCase, UpdateBranchNameUseCase updateBranchNameUseCase) {
        this.createBranchUseCase = createBranchUseCase;
        this.updateBranchNameUseCase = updateBranchNameUseCase;
    }

    @Operation(summary = "Create branch", description = "Creates a new branch for a franchise")
    @ApiResponse(responseCode = "201", description = "Branch created successfully", content = @Content(schema = @Schema(implementation = BranchResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise not found")
    @ApiResponse(responseCode = "409", description = "Duplicate branch name")
    public Mono<ServerResponse> create(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        return request.bodyToMono(BranchRequest.class)
                .flatMap(branchRequest -> {
                    Branch branch = BranchMapper.toDomain(branchRequest, franchiseId);
                    return createBranchUseCase.create(branch);
                })
                .flatMap(branch -> {
                    var response = BranchMapper.toResponse(branch);
                    return ServerResponse.status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }

    @Operation(summary = "Update branch name", description = "Updates the name of an existing branch")
    @ApiResponse(responseCode = "200", description = "Name updated successfully", content = @Content(schema = @Schema(implementation = BranchResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise or branch not found")
    @ApiResponse(responseCode = "409", description = "Duplicate branch name")
    public Mono<ServerResponse> updateName(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        Long branchId = Long.parseLong(request.pathVariable("branchId"));
        return request.bodyToMono(BranchRequest.class)
                .flatMap(branchRequest -> updateBranchNameUseCase.updateName(branchId, franchiseId, branchRequest.name()))
                .flatMap(branch -> {
                    var response = BranchMapper.toResponse(branch);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }
}

