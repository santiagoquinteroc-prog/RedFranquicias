package com.red.franquicias.application.usecase.branch;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.exception.ValidationException;
import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.domain.validator.BranchValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UpdateBranchNameUseCaseImpl implements UpdateBranchNameUseCase {
    private final BranchRepositoryPort branchRepositoryPort;
    private final FranchiseRepositoryPort franchiseRepositoryPort;

    public UpdateBranchNameUseCaseImpl(BranchRepositoryPort branchRepositoryPort, FranchiseRepositoryPort franchiseRepositoryPort) {
        this.branchRepositoryPort = branchRepositoryPort;
        this.franchiseRepositoryPort = franchiseRepositoryPort;
    }

    @Override
    public Mono<Branch> updateName(Long branchId, Long franchiseId, String name) {
        try {
            BranchValidator.validateName(name);
        } catch (IllegalArgumentException e) {
            return Mono.error(new ValidationException(e.getMessage()));
        }

        return franchiseRepositoryPort.findById(franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franchise not found")))
                .flatMap(franchise -> branchRepositoryPort.findByIdAndFranchiseId(branchId, franchiseId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Branch not found or does not belong to franchise")))
                        .flatMap(existing -> branchRepositoryPort.existsByNameAndFranchiseId(name, franchiseId)
                                .flatMap(exists -> {
                                    if (exists && !existing.getName().equals(name)) {
                                        return Mono.error(new ConflictException("Branch name already exists in this franchise"));
                                    }
                                    existing.setName(name);
                                    return branchRepositoryPort.save(existing);
                                })));
    }
}

