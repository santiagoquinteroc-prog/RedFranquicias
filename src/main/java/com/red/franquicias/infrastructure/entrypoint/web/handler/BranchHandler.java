package com.red.franquicias.infrastructure.entrypoint.web.handler;

import com.red.franquicias.application.usecase.branch.CreateBranchUseCase;
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

    public BranchHandler(CreateBranchUseCase createBranchUseCase) {
        this.createBranchUseCase = createBranchUseCase;
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
}

