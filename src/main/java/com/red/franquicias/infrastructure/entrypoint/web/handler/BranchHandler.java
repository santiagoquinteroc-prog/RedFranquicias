package com.red.franquicias.infrastructure.entrypoint.web.handler;

import com.red.franquicias.application.usecase.branch.CreateBranchUseCase;
import com.red.franquicias.application.usecase.branch.UpdateBranchNameUseCase;
import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.infrastructure.entrypoint.web.dto.BranchRequest;
import com.red.franquicias.infrastructure.entrypoint.web.mapper.BranchMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class BranchHandler {
    private final CreateBranchUseCase createBranchUseCase;
    private final UpdateBranchNameUseCase updateBranchNameUseCase;

    public BranchHandler(CreateBranchUseCase createBranchUseCase, UpdateBranchNameUseCase updateBranchNameUseCase) {
        this.createBranchUseCase = createBranchUseCase;
        this.updateBranchNameUseCase = updateBranchNameUseCase;
    }

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

    public Mono<ServerResponse> updateName(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        Long branchId = Long.parseLong(request.pathVariable("branchId"));
        return request.bodyToMono(BranchRequest.class)
                .flatMap(branchRequest -> updateBranchNameUseCase.updateName(branchId, franchiseId, branchRequest.getName()))
                .flatMap(branch -> {
                    var response = BranchMapper.toResponse(branch);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }
}

