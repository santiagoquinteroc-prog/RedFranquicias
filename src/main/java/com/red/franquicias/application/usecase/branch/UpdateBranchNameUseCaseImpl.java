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
public class UpdateBranchNameUseCaseImpl implements UpdateBranchNameUseCase {

    private final BranchRepositoryPort branchRepositoryPort;
    private final FranchiseRepositoryPort franchiseRepositoryPort;

    public UpdateBranchNameUseCaseImpl(
            BranchRepositoryPort branchRepositoryPort,
            FranchiseRepositoryPort franchiseRepositoryPort
    ) {
        this.branchRepositoryPort = branchRepositoryPort;
        this.franchiseRepositoryPort = franchiseRepositoryPort;
    }

    @Override
    public Mono<Branch> updateName(
            Long branchId,
            Long franchiseId,
            String name
    ) {
        return franchiseRepositoryPort.findById(franchiseId)
                .switchIfEmpty(Mono.error(
                        new BusinessException(TechnicalMessage.FRANCHISE_NOT_FOUND)
                ))
                .flatMap(franchise ->
                        branchRepositoryPort.findByIdAndFranchiseId(branchId, franchiseId)
                                .switchIfEmpty(Mono.error(
                                        new BusinessException(TechnicalMessage.BRANCH_NOT_FOUND)
                                ))
                                .flatMap(existing ->
                                        branchRepositoryPort.existsByNameAndFranchiseId(name, franchiseId)
                                                .flatMap(exists -> {
                                                    if (exists && !existing.getName().equals(name)) {
                                                        return Mono.error(new BusinessException(
                                                                TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS
                                                        ));
                                                    }
                                                    existing.setName(name);
                                                    return branchRepositoryPort.save(existing);
                                                })
                                )
                )
                .onErrorMap(
                        ex -> !(ex instanceof BusinessException),
                        ex -> new TechnicalException(ex, TechnicalMessage.BRANCH_UPDATE_ERROR)
                );
    }
}
