package com.red.franquicias.application.usecase.branch;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.model.Branch;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Service
@Validated
public class CreateBranchUseCaseImpl implements CreateBranchUseCase {

    private final BranchRepositoryPort branchRepositoryPort;
    private final FranchiseRepositoryPort franchiseRepositoryPort;

    public CreateBranchUseCaseImpl(
            BranchRepositoryPort branchRepositoryPort,
            FranchiseRepositoryPort franchiseRepositoryPort
    ) {
        this.branchRepositoryPort = branchRepositoryPort;
        this.franchiseRepositoryPort = franchiseRepositoryPort;
    }

    @Override
    public Mono<Branch> create(Branch branch) {

        return franchiseRepositoryPort.findById(branch.getFranchiseId())
                .switchIfEmpty(Mono.error(new NotFoundException("Franchise not found")))
                .flatMap(franchise ->
                        branchRepositoryPort.existsByNameAndFranchiseId(
                                        branch.getName(),
                                        branch.getFranchiseId()
                                )
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(
                                                new ConflictException(
                                                        "Branch name already exists in this franchise"
                                                )
                                        );
                                    }
                                    return branchRepositoryPort.save(branch);
                                })
                );
    }
}
