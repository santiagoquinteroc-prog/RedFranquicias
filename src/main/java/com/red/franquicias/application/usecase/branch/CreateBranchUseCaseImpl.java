package com.red.franquicias.application.usecase.branch;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.enums.TechnicalMessage;
import com.red.franquicias.domain.exception.BusinessException;
import com.red.franquicias.domain.exception.TechnicalException;
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
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.FRANCHISE_NOT_FOUND)))
                .flatMap(franchise ->
                        branchRepositoryPort.existsByNameAndFranchiseId(branch.getName(), branch.getFranchiseId())
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new BusinessException(
                                                TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS
                                        ));
                                    }
                                    return branchRepositoryPort.save(branch);
                                })
                )
                .onErrorMap(
                        ex -> !(ex instanceof BusinessException),
                        ex -> new TechnicalException(ex, TechnicalMessage.BRANCH_CREATE_ERROR)
                );
    }
}
