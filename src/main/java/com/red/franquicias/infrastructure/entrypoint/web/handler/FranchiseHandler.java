package com.red.franquicias.infrastructure.entrypoint.web.handler;

import com.red.franquicias.application.usecase.franchise.CreateFranchiseUseCase;
import com.red.franquicias.application.usecase.franchise.UpdateFranchiseNameUseCase;
import com.red.franquicias.domain.exception.BusinessException;
import com.red.franquicias.domain.exception.TechnicalException;
import com.red.franquicias.domain.model.Franchise;
import com.red.franquicias.infrastructure.entrypoint.web.dto.FranchiseRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.FranchiseResponse;
import com.red.franquicias.infrastructure.entrypoint.web.mapper.FranchiseMapper;
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
@Tag(name = "Franchises", description = "API for franchise management")
public class FranchiseHandler {

    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final UpdateFranchiseNameUseCase updateFranchiseNameUseCase;
    private final Validator validator;

    @Operation(summary = "Create franchise", description = "Creates a new franchise")
    @ApiResponse(responseCode = "201", description = "Franchise created successfully",
            content = @Content(schema = @Schema(implementation = FranchiseResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "409", description = "Duplicate franchise name")
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(FranchiseRequest.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Body is required")))
                .flatMap(franchiseRequest -> {
                    WebHandlerUtils.validate(validator, franchiseRequest);
                    Franchise franchise = FranchiseMapper.toDomain(franchiseRequest);
                    return createFranchiseUseCase.create(franchise);
                })
                .doOnSuccess(franchise -> log.info("Franchise created successfully: {}", franchise.getId()))
                .flatMap(franchise ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(FranchiseMapper.toResponse(franchise))
                )
                .doOnError(ex -> log.error("Error creating franchise", ex))
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
                    log.error("Unexpected error creating franchise", ex);
                    return WebHandlerUtils.error(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                });
    }

    @Operation(summary = "Update franchise name", description = "Updates the name of an existing franchise")
    @ApiResponse(responseCode = "200", description = "Name updated successfully",
            content = @Content(schema = @Schema(implementation = FranchiseResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Franchise not found")
    @ApiResponse(responseCode = "409", description = "Duplicate franchise name")
    public Mono<ServerResponse> updateName(ServerRequest request) {
        return WebHandlerUtils.parseLongPathVariable(request, "id")
                .flatMap(id ->
                        request.bodyToMono(FranchiseRequest.class)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Body is required")))
                                .flatMap(franchiseRequest -> {
                                    WebHandlerUtils.validate(validator, franchiseRequest);
                                    return updateFranchiseNameUseCase.updateName(id, franchiseRequest.name());
                                })
                                .doOnSuccess(franchise -> log.info("Franchise name updated successfully: {}", franchise.getId()))
                )
                .flatMap(franchise ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(FranchiseMapper.toResponse(franchise))
                )
                .doOnError(ex -> log.error("Error updating franchise name", ex))
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
                    log.error("Unexpected error updating franchise name", ex);
                    return WebHandlerUtils.error(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                });
    }
}
