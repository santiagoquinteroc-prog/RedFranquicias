package com.red.franquicias.infrastructure.entrypoint.web.handler;

import com.red.franquicias.application.usecase.branch.CreateBranchUseCase;
import com.red.franquicias.application.usecase.branch.UpdateBranchNameUseCase;
import com.red.franquicias.domain.exception.BusinessException;
import com.red.franquicias.domain.exception.TechnicalException;
import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.infrastructure.entrypoint.web.dto.BranchRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.BranchResponse;
import com.red.franquicias.infrastructure.entrypoint.web.mapper.BranchMapper;
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
@Tag(name = "Branches", description = "API for branch management")
public class BranchHandler {

    private final CreateBranchUseCase createBranchUseCase;
    private final UpdateBranchNameUseCase updateBranchNameUseCase;
    private final Validator validator;

    @Operation(summary = "Create branch", description = "Creates a new branch for a franchise")
    @ApiResponse(responseCode = "201", description = "Branch created successfully",
            content = @Content(schema = @Schema(implementation = BranchResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise not found")
    @ApiResponse(responseCode = "409", description = "Duplicate branch name")
    public Mono<ServerResponse> create(ServerRequest request) {
        return WebHandlerUtils.parseLongPathVariable(request, "franchiseId")
                .flatMap(franchiseId ->
                        request.bodyToMono(BranchRequest.class)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Body is required")))
                                .flatMap(branchRequest -> {
                                    WebHandlerUtils.validate(validator, branchRequest);
                                    Branch branch = BranchMapper.toDomain(branchRequest, franchiseId);
                                    return createBranchUseCase.create(branch);
                                })
                                .doOnSuccess(branch -> log.info("Branch created successfully: {}", branch.getId()))
                )
                .flatMap(branch ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(BranchMapper.toResponse(branch))
                )
                .doOnError(ex -> log.error("Error creating branch", ex))
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
                    log.error("Unexpected error creating branch", ex);
                    return WebHandlerUtils.error(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                });
    }

    @Operation(summary = "Update branch name", description = "Updates the name of an existing branch")
    @ApiResponse(responseCode = "200", description = "Name updated successfully",
            content = @Content(schema = @Schema(implementation = BranchResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise or branch not found")
    @ApiResponse(responseCode = "409", description = "Duplicate branch name")
    public Mono<ServerResponse> updateName(ServerRequest request) {
        return Mono.zip(
                        WebHandlerUtils.parseLongPathVariable(request, "franchiseId"),
                        WebHandlerUtils.parseLongPathVariable(request, "branchId")
                )
                .flatMap(tuple ->
                        request.bodyToMono(BranchRequest.class)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Body is required")))
                                .flatMap(branchRequest -> {
                                    WebHandlerUtils.validate(validator, branchRequest);
                                    return updateBranchNameUseCase.updateName(
                                            tuple.getT2(),
                                            tuple.getT1(),
                                            branchRequest.name()
                                    );
                                })
                                .doOnSuccess(branch -> log.info("Branch name updated successfully: {}", branch.getId()))
                )
                .flatMap(branch ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(BranchMapper.toResponse(branch))
                )
                .doOnError(ex -> log.error("Error updating branch name", ex))
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
                    log.error("Unexpected error updating branch name", ex);
                    return WebHandlerUtils.error(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                });
    }
}
