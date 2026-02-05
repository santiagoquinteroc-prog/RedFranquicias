package com.red.franquicias.infrastructure.entrypoint.web.handler;

import com.red.franquicias.application.usecase.franchise.CreateFranchiseUseCase;
import com.red.franquicias.application.usecase.franchise.UpdateFranchiseNameUseCase;
import com.red.franquicias.domain.model.Franchise;
import com.red.franquicias.infrastructure.entrypoint.web.dto.FranchiseRequest;
import com.red.franquicias.infrastructure.entrypoint.web.mapper.FranchiseMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class FranchiseHandler {
    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final UpdateFranchiseNameUseCase updateFranchiseNameUseCase;

    public FranchiseHandler(CreateFranchiseUseCase createFranchiseUseCase, UpdateFranchiseNameUseCase updateFranchiseNameUseCase) {
        this.createFranchiseUseCase = createFranchiseUseCase;
        this.updateFranchiseNameUseCase = updateFranchiseNameUseCase;
    }

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

    public Mono<ServerResponse> updateName(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(FranchiseRequest.class)
                .flatMap(franchiseRequest -> updateFranchiseNameUseCase.updateName(id, franchiseRequest.getName()))
                .flatMap(franchise -> {
                    var response = FranchiseMapper.toResponse(franchise);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }
}


